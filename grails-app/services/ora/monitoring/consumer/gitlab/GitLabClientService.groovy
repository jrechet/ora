package ora.monitoring.consumer.gitlab

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Slf4j
@Service
class GitLabClientService implements GrailsConfigurationAware {

    @Autowired
    RestTemplate restTemplate

    String gitlabUrl
    String gitlabToken

    @Override
    void setConfiguration(Config co) {
        gitlabUrl = co.getProperty('gitlab.url', String)
        gitlabToken = co.getProperty('gitlab.token', String)
    }

    GitLabTestsStatus getTestsStatus(String gitlabRepositoryUrl, String projectIdentifier) {
        try {
            def projectId = projectIdentifier.contains('/') ?
                    URLEncoder.encode(projectIdentifier, 'UTF-8') :
                    projectIdentifier

            def headers = new HttpHeaders()
            headers.set('PRIVATE-TOKEN', gitlabToken)
            def entity = new HttpEntity<>(headers)

            // 1. Récupérer les derniers pipelines sur develop
            def pipelineUrl = "${gitlabUrl}/api/v4/projects/${projectId}/pipelines?ref=develop&per_page=20"
            def responseType = new ParameterizedTypeReference<List<GitLabPipelineResponse>>() {}

            def response = restTemplate.exchange(
                    pipelineUrl,
                    HttpMethod.GET,
                    entity,
                    responseType
            )

            def pipelines = response.body
            if (!pipelines) {
                log.warn("Aucun pipeline trouvé pour le projet ${projectId}")
                return new GitLabTestsStatus(
                        jobs: [],
                        pipelineUrl: "${gitlabUrl}/${projectId}/-/pipelines"
                )
            }

            // 2. Trouver le dernier pipeline avec des jobs de test terminés
            GitLabPipelineResponse targetPipeline = null
            def testJobs = []

            for (pipeline in pipelines) {
                def jobsUrl = "${gitlabUrl}/api/v4/projects/${projectId}/pipelines/${pipeline.id}/jobs"
                def jobsResponseType = new ParameterizedTypeReference<List<GitLabJobResponse>>() {}

                def jobsResponse = restTemplate.exchange(
                        jobsUrl,
                        HttpMethod.GET,
                        entity,
                        jobsResponseType
                )

                testJobs = jobsResponse.body?.findAll { it.stage == 'test' }

                if (testJobs && testJobsAreCompleted(testJobs)) {
                    targetPipeline = pipeline
                    break
                }
            }

            if (!targetPipeline || !testJobs) {
                log.warn("Aucun pipeline avec jobs de test terminés trouvé pour le projet ${projectId}")
                return new GitLabTestsStatus(
                        jobs: [],
                        pipelineUrl: "${gitlabRepositoryUrl}/-/pipelines"
                )
            }

            log.debug("Jobs trouvés: ${testJobs.collect { "${it.stage}/${it.name}" }}")

            // Convertir tous les jobs de test en objets Job
            def monitoredJobs = testJobs.collect { gitlabJob ->
                new Job(
                        name: gitlabJob.name,
                        success: evaluateJobStatus(gitlabJob),
                        url: gitlabJob.web_url
                )
            }

            def status = new GitLabTestsStatus(
                    jobs: monitoredJobs,
                    pipelineUrl: targetPipeline.web_url
            )

            log.debug("Statut des tests: ${monitoredJobs.collect { "${it.name}=${it.success}" }.join(', ')}")
            return status

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Erreur d'authentification GitLab - Token invalide ou expiré")
            return createFailedStatus("Erreur d'authentification GitLab")
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du statut des tests sur GitLab", e)
            return createFailedStatus("Erreur technique")
        }
    }

    /**
     * Vérifie si tous les jobs de test sont terminés (succès ou échec)
     */
    private static boolean testJobsAreCompleted(List<GitLabJobResponse> jobs) {
        return jobs.every { job ->
            !['running', 'pending', 'created'].contains(job.status)
        }
    }

    private static GitLabTestsStatus createFailedStatus(String message = null) {
        return new GitLabTestsStatus(
                jobs: [],
                pipelineUrl: null,
                error: message
        )
    }
    /**
     * Évalue le statut d'un job
     * @param job le job à évaluer (peut être null)
     * @return true si le job existe et est en succès, false sinon
     */
    private boolean evaluateJobStatus(GitLabJobResponse job) {
        if (!job) {
            log.debug("Job non trouvé")
            return false
        }

        if (job.isFailed()) {
            log.debug("Job en échec: ${job.failure_reason}")
            return false
        }

        if (job.isRunning() || job.isPending()) {
            log.debug("Job en cours ou en attente")
            return false
        }

        return job.isSuccess()
    }
}