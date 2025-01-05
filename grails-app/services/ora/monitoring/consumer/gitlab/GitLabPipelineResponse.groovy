package ora.monitoring.consumer.gitlab

import groovy.transform.ToString

@ToString(includeNames = true)
class GitLabPipelineResponse {
    Long id
    String status
    String ref
    String sha
    String web_url
    List<Stage> stages

    static class Stage {
        String name
        String status

        boolean isCompleted() {
            return status == 'success' || status == 'failed'
        }

        boolean isRunning() {
            return status == 'running' || status == 'pending' || status == 'created'
        }
    }
}