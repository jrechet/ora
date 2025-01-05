package ora.monitoring.apps

/**
 * The dev team project.
 */
class Project {
    String name
    String repositoryUrl
    String gitlabProjectId

    static constraints = {
        name blank: false
        repositoryUrl nullable: true
        gitlabProjectId nullable: true
    }
}
