package ora.monitoring

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import ora.ApplicationsConfig
import ora.monitoring.apps.ApplicationInstance
import ora.monitoring.apps.Environment
import ora.monitoring.apps.Project
import ora.monitoring.consumer.codecoverage.CodeCoverageClientService
import ora.monitoring.consumer.gitlab.GitLabClientService
import ora.monitoring.consumer.healthcheck.HealthcheckClientService
import ora.monitoring.consumer.supervision.SupervisionClientService
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

@Slf4j
@Transactional
class MonitoringService implements GrailsConfigurationAware {

    @Autowired
    ApplicationsConfig applicationsConfig

    HealthcheckClientService healthcheckClientService
    CodeCoverageClientService codeCoverageClientService
    SupervisionClientService supervisionClientService
    GitLabClientService gitLabClientService

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    Map apps
    Map envs

    @Override
    void setConfiguration(Config co) {
        def appConfig = applicationsConfig.loadApplicationsConfig()
        def env = grails.util.Environment.current.name

        apps = appConfig.getProperty("environments.${env}.applications.monitoring.apps", Map) ?: appConfig.getProperty("environments.development.applications.monitoring.apps", Map)
        envs = appConfig.getProperty("environments.${env}.applications.monitoring.envs", Map) ?: appConfig.getProperty("environments.development.applications.monitoring.envs", Map)
    }

    @PostConstruct
    void init() {
        initializeEnvironmentConfigurations()
        initializeApplications()
    }

    /**
     * Initialise les configurations d'environnement à partir de la configuration.
     */
    @Transactional
    void initializeEnvironmentConfigurations() {
        envs.each { logicalEnvName, envConfig ->
            def level = envConfig.params.level as Integer
            envConfig.tenants.each { tenantName, tenantConfig ->
                Environment env = Environment.findByLogicalNameAndTenant(logicalEnvName, tenantName) ?:
                        new Environment(logicalName: logicalEnvName, tenant: tenantName)
                env.level = level
                if (!env.save(flush: true)) {
                    log.error("Error saving environment ${logicalEnvName}-${tenantName}: ${env.errors}")
                }
            }
        }
    }

    /**
     * Initialise les applications à partir de la configuration.
     */
    @Transactional
    def initializeApplications() {
        apps.each { projectName, projectConfig ->
            // Créer et sauvegarder le projet d'abord
            Project project = Project.findByName(projectName)
            if (!project) {
                project = new Project(name: projectName)
            }
            project.repositoryUrl = projectConfig.global.repositoryUrl
            project.gitlabProjectId = projectConfig.global.gitlabProjectId

            if (!project.save(flush: true)) {
                log.error("Error saving project ${projectName}: ${project.errors}")
                return
            }

            projectConfig.instances.each { envName, envConfig ->
                envConfig.each { tenantName, tenantConfig ->
                    Environment env = Environment.findByLogicalNameAndTenant(envName, tenantName)
                    if (!env) {
                        env = new Environment(logicalName: envName, tenant: tenantName).save()
                        log.error("Environment ${envName} not found for project ${projectName}")
                    }
                    // Chercher une instance existante
                    def instance = ApplicationInstance.findByProjectAndEnvironmentAndTenant(project, env, tenantName)

                    if (!instance) {
                        instance = new ApplicationInstance(
                                project: project,
                                environment: env,
                                tenant: tenantName
                        )
                    }

                    // Mettre à jour les URLs
                    instance.baseUrl = tenantConfig.baseUrl
                    instance.healthUrl = buildUrl(tenantConfig.baseUrl, 'healthUrl', projectConfig.global, tenantConfig)
                    instance.supervisionUrl = buildUrl(tenantConfig.baseUrl, 'supervisionUrl', projectConfig.global, tenantConfig)
                    instance.codeCoverageUrl = buildUrl(tenantConfig.baseUrl, 'codeCoverageUrl', projectConfig.global, tenantConfig)
                    instance.logsUrl = tenantConfig.logsUrl

                    if (!instance.save(flush: true)) {
                        log.error("Error saving instance for ${projectName}/${envName}/${tenantName}: ${instance.errors}")
                    }
                }
            }
        }
    }

    def buildUrl(String baseUrl, String key, globalConfig, tenantConfig) {
        def path = tenantConfig[key] ?: globalConfig[key]
        path ? baseUrl + path : null
    }

    /**
     * Récupère la structure des applications.
     */
    def getApplicationStructure() {
        def structure = [:].withDefault { [] }

        ApplicationInstance.list().each { instance ->
            def env = instance.environment
            structure[env.id] << [
                    name            : instance.project.name,
                    id              : instance.id,
                    baseUrl         : instance.baseUrl,
                    healthUrl       : instance.healthUrl,
                    supervisionUrl  : instance.supervisionUrl,
                    repositoryUrl   : instance.project.repositoryUrl,
                    logsUrl         : instance.logsUrl,
                    environment     : env.logicalName,
                    tenant          : env.tenant,
                    environmentLevel: env.level
            ]
        }

        return structure
    }

    /**
     * Récupère le statut des applications pour un environnement donné
     */
    def refreshApplicationsStatus(Long envId) {
        def environment = Environment.get(envId)
        if (!environment) {
            log.error("Environment not found: ${envId}")
            return []
        }

        def applications = ApplicationInstance.findAllByEnvironment(environment)

        // 1. Lancer en parallèle les appels à GitLab
        def projectFutures = ApplicationInstance.list()
                .collect { it.project }
                .unique()
                .findAll { it.gitlabProjectId }
                .collectEntries { project ->
                    [
                            project.gitlabProjectId,
                            CompletableFuture.supplyAsync {
                                gitLabClientService.getTestsStatus(project.repositoryUrl, project.gitlabProjectId)
                            }
                    ]
                }

        // 2. Lancer en parallèle le traitement des applications
        def applicationFutures = applications.collect { instance ->
            CompletableFuture.supplyAsync {
                [
                        instance   : instance,
                        health     : healthcheckClientService.checkHealth(instance.healthUrl),
                        supervision: supervisionClientService.checkSupervision(instance.supervisionUrl),
                        coverage   : instance.codeCoverageUrl ? fetchCodeCoverage(instance) : null
                ]
            }
        }

        // 3. Attendre que tous les futures soient terminés
        CompletableFuture.allOf(
                (projectFutures.values() + applicationFutures) as CompletableFuture[]
        ).join()

        // 4. Récupérer les résultats GitLab
        def testStatusByProject = projectFutures.collectEntries { projectId, future ->
            [projectId, future.get()]
        }

        // 5. Construire le résultat final
        return applicationFutures.collect { future ->
            def result = future.get()
            def instance = result.instance
            def testStatus = testStatusByProject[instance.project.gitlabProjectId]

            [
                    name             : instance.project.name,
                    id               : instance.id,
                    status           : result.health,
                    supervisionReport: result.supervision,
                    baseUrl          : instance.baseUrl,
                    healthUrl        : instance.healthUrl,
                    supervisionUrl   : instance.supervisionUrl,
                    testJobs         : testStatus?.jobs ?: [],
                    pipelineUrl      : testStatus?.pipelineUrl,
                    tenant           : instance.tenant,
                    logsUrl          : instance.logsUrl,
                    lastChecked      : LocalDateTime.now().format(FORMATTER),
                    coverage         : result.coverage
            ]
        }
    }

    private Map fetchCodeCoverage(ApplicationInstance instance) {
        try {
            def coverage = codeCoverageClientService.getCodeCoverage(instance.codeCoverageUrl)
            return [
                    totalCoverage: coverage.totalCoverage,
                    details      : coverage.fileDetails.collect { detail ->
                        [
                                file        : detail.file,
                                coverage    : detail.coverage,
                                coveredLines: detail.coveredLines,
                                totalLines  : detail.totalLines
                        ]
                    }
            ]
        } catch (Exception e) {
            log.error("Error fetching code coverage for ${instance.project.name}", e)
            return null
        }
    }

    /**
     * Récupère un résumé des applications en panne par environnement
     */
    def getSummary(Map<String, List> statusByEnvironment) {
        def totalDown = 0
        def envsWithIssues = [:].withDefault { 0 }

        statusByEnvironment.each { envName, apps ->
            apps.each { app ->
                if (!app.status) {
                    totalDown++
                    envsWithIssues[envName]++
                }
            }
        }

        def problematicEnvs = envsWithIssues.findAll { it.value > 0 }

        [
                totalDown      : totalDown,
                problematicEnvs: problematicEnvs
        ]
    }

}