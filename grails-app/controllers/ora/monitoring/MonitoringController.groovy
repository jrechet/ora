package ora.monitoring

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import ora.monitoring.apps.ApplicationInstance
import ora.monitoring.consumer.http.HttpClientDelegateService

import java.time.format.DateTimeFormatter

@Secured(['ROLE_ADMIN', 'ROLE_USER'])
class MonitoringController {

    def monitoringService
    def monitoringCacheHandlerService
    def gitLabClientService
    HttpClientDelegateService httpClientDelegateService

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    def index() {
        // Vérifier la disponibilité des modes
        boolean serverToServerAvailable = monitoringCacheHandlerService?.isAvailable()

        def model = monitoringService.getApplicationStructure()
        [
                applicationStructure   : model,
                summary                : [:],
                refreshInterval        : grailsApplication.config.getProperty(
                        'monitoring.refresh.interval',
                        Integer,
                        30000
                ),
                serverToServerAvailable: serverToServerAvailable,
                currentMode            : httpClientDelegateService.mode
        ]
    }

    def setMode() {
        def mode = params.mode
        try {
            httpClientDelegateService.setMode(mode)
            render([success: true, mode: mode] as JSON)
        } catch (Exception e) {
            log.error "Error setting mode", e
            render([success: false, error: e.message] as JSON)
        }
    }

    def status(Long envId) {
        log.info "Received status request for env: ${envId}"

        if (!envId) {
            render([error: "Environment is required"] as JSON)
            return
        }

        def statusData = monitoringCacheHandlerService.getStatus(envId)
        def model = [
                applicationsStatus: statusData,
                summary           : monitoringService.getSummary([envId: statusData])
        ]

        render model as JSON
    }

    def summary() {
        def statusByEnv = monitoringCacheHandlerService.status
        def summaryData = monitoringService.getSummary(statusByEnv)
        render summaryData as JSON
    }

    def gitlabStatus(Long id) {
        def app = ApplicationInstance.get(id)
        if (!app?.project?.gitlabProjectId) {
            render([success: false, error: "No GitLab configuration found"] as JSON)
            return
        }

        def status = gitLabClientService.getTestsStatus(app.project.repositoryUrl, app.project.gitlabProjectId)
        render status as JSON
    }

    def synchronize() {
        log.info "Force refresh all requested"
        monitoringCacheHandlerService.refreshCache()
        render([success: true] as JSON)
    }
}
