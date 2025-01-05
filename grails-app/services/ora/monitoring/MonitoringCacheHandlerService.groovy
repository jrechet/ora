package ora.monitoring

import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import ora.monitoring.apps.ApplicationInstance
import ora.monitoring.apps.Environment
import org.springframework.scheduling.annotation.Scheduled

import javax.annotation.PostConstruct
import java.util.concurrent.CompletableFuture

@Slf4j
@Transactional
/**
 * Service qui gère le cache des données de monitoring.
 */
class MonitoringCacheHandlerService {
    static lazyInit = false
    def monitoringService
    def httpClientDelegateService

    private volatile Map<String, List> statusCache = [:]
    private final Map<String, Object> locks = [:].withDefault { new Object() }
    private final Object globalLock = new Object()

    private volatile long lastSuccessfulUpdate = 0
    private volatile int consecutiveFailures = 0
    private static final int MAX_CONSECUTIVE_FAILURES = 3
    private static final long CACHE_VALIDITY_PERIOD = 120000 // 2 minutes

    @PostConstruct
    void init() {
        if (!isAutonomousMode()) {
            CompletableFuture.runAsync {
                refreshCache()
            }
        }
    }

    private boolean isAutonomousMode() {
        return httpClientDelegateService.currentMode.toString() == 'autonomous'
    }

    @NotTransactional
    @Scheduled(fixedRateString = '${monitoring.cache.refresh-rate:60000}')
    void refreshCache() {
        if (isAutonomousMode()) {
            log.debug "Cache refresh skipped: autonomous mode is enabled"
            return
        }

        synchronized (globalLock) {
            log.info "Starting global cache refresh"
            try {
                def structure = monitoringService.getApplicationStructure()

                List<CompletableFuture<Void>> futures = structure.keySet().collect { key ->
                    CompletableFuture.runAsync {
                        updateCache(key)
                    }
                }

                CompletableFuture.allOf(futures as CompletableFuture[]).join()

                log.info "Global cache refresh completed"
            } catch (Exception e) {
                log.error "Error during global cache refresh", e
            }
        }
    }

    private String normalizeKey(String env, String tenant) {
        def (logicalName, envTenant) = parseEnvironmentName(env)

        // Chercher d'abord l'environnement exact
        def environment = Environment.findByLogicalNameAndTenant(logicalName, envTenant)
        if (!environment) {
            log.error "Environment not found: ${logicalName}-${envTenant}"
            return null
        }

        // Chercher le tenant exact pour cet environnement
        def instance = ApplicationInstance.findByEnvironmentAndTenant(environment, tenant)
        if (!instance) {
            // Essayer de trouver un tenant qui correspond en ignorant la casse
            def allInstances = ApplicationInstance.findAllByEnvironment(environment)
            instance = allInstances.find { it.tenant.equalsIgnoreCase(tenant) }
            if (!instance) {
                log.error "Tenant not found for env ${environment.key}: ${tenant}"
                return null
            }
        }

        // Retourner la clé de l'environnement
        return environment.key
    }

    private static def parseEnvironmentName(String envName) {
        def parts = envName.split('-', 2)
        return parts.length > 1 ? [parts[0], parts[1]] : [parts[0], "default"]
    }

    @NotTransactional
    Map<String, List> getStatus() {
        if (isAutonomousMode()) {
            log.debug "Returning empty status: autonomous mode is enabled"
            return [:]
        }

        if (statusCache.isEmpty()) {
            synchronized (globalLock) {
                if (statusCache.isEmpty()) {
                    log.info "Cache is empty, forcing initial load"
                    def structure = monitoringService.getApplicationStructure()
                    structure.keySet().each { key ->
                        updateCache(key as String)
                    }
                }
            }
        }
        return new HashMap<>(statusCache)
    }

    @NotTransactional
    List getStatus(Long envId) {
        if (isAutonomousMode()) {
            log.debug "Returning empty status for env ${envId}: autonomous mode is enabled"
            return []
        }

        if (!statusCache.containsKey(envId)) {
            synchronized (locks[envId]) {
                if (!statusCache.containsKey(envId)) {
                    log.info "Cache miss for env ${envId}, loading data"
                    updateCache(envId)
                }
            }
        }
        return new ArrayList<>(statusCache[envId] ?: [])
    }


    @NotTransactional
    private void updateCache(Long envId) {
        if (isAutonomousMode()) {
            log.debug "Cache update skipped for env ${envId}: autonomous mode is enabled"
            return
        }

        if (envId && locks[envId]) {
            synchronized (locks[envId]) {
                try {
                    log.info "Updating cache for env ${envId}"
                    def newStatus = monitoringService.refreshApplicationsStatus(envId)
                    lastSuccessfulUpdate = System.currentTimeMillis()
                    consecutiveFailures = 0
                    statusCache[envId] = newStatus
                    log.info "Cache updated successfully for env ${envId}"
                } catch (Exception e) {
                    log.error "Error updating cache for env ${envId}", e
                    synchronized (locks[envId]) {
                        consecutiveFailures++
                    }
                }
            }
        }
    }

    boolean isAvailable() {
        return true
    }
}