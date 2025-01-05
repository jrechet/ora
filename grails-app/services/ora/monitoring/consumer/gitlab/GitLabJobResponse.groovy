package ora.monitoring.consumer.gitlab

import groovy.transform.ToString

@ToString(includeNames = true)
class GitLabJobResponse {
    Long id
    String name
    String stage
    String status
    String ref
    String failure_reason
    Pipeline pipeline
    String web_url

    static class Pipeline {
        Long id
        Long project_id
        String ref
        String sha
        String status
    }

    boolean isSuccess() {
        return status == 'success'
    }

    boolean isFailed() {
        return status == 'failed'
    }

    boolean isRunning() {
        return status == 'running'
    }

    boolean isPending() {
        return status == 'pending'
    }
}