package ora.monitoring.apps

/**
 * Deployed application instance.
 */
class ApplicationInstance {
    String tenant
    String baseUrl
    String healthUrl
    String supervisionUrl
    String codeCoverageUrl
    String logsUrl

    static belongsTo = [environment: Environment, project: Project]

    static constraints = {
        tenant nullable: false
        baseUrl nullable: true
        healthUrl nullable: true
        supervisionUrl nullable: true
        codeCoverageUrl nullable: true
        logsUrl nullable: true
    }
}
