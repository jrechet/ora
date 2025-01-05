package ora.monitoring.consumer.gitlab

import groovy.transform.ToString

@ToString(includeNames = true)
class GitLabTestsStatus {
    List<Job> jobs
    String pipelineUrl
    String error
}