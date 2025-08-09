package ora.monitoring

import grails.testing.gorm.DataTest
import ora.ApplicationsConfigService
import ora.monitoring.apps.ApplicationInstance
import ora.monitoring.apps.Environment
import ora.monitoring.apps.Project
import ora.monitoring.consumer.codecoverage.CodeCoverageClientService
import ora.monitoring.consumer.gitlab.GitLabClientService
import ora.monitoring.consumer.healthcheck.HealthcheckClientService
import ora.monitoring.consumer.supervision.SupervisionClientService
import spock.lang.DetachedMockFactory
import spock.lang.Specification

class MonitoringServiceSpec extends Specification implements DataTest {

    // The service under test
    MonitoringService service

    // Mocks for dependencies that need to be accessed in the test methods
    HealthcheckClientService healthcheckClientService
    SupervisionClientService supervisionClientService
    GitLabClientService gitLabClientService

    void setup() {
        // We need to mock the domains for GORM operations
        mockDomains(Project, Environment, ApplicationInstance)

        // Use a DetachedMockFactory to create mocks that are safe to use across threads
        def factory = new DetachedMockFactory()

        // Create mocks for all service dependencies
        def applicationsConfigService = factory.Mock(ApplicationsConfigService)
        healthcheckClientService = factory.Mock(HealthcheckClientService)
        supervisionClientService = factory.Mock(SupervisionClientService)
        gitLabClientService = factory.Mock(GitLabClientService)
        def codeCoverageClientService = factory.Mock(CodeCoverageClientService)

        // Create the service instance manually
        service = new MonitoringService(
            applicationsConfigService: applicationsConfigService,
            healthcheckClientService: healthcheckClientService,
            codeCoverageClientService: codeCoverageClientService,
            supervisionClientService: supervisionClientService,
            gitLabClientService: gitLabClientService
        )
    }

    void "test initializeEnvironmentConfigurations"() {
        given:
        def envs = [
                DEV: [
                        params : [level: 1],
                        tenants: [
                                tenant1: [:]
                        ]
                ]
        ]
        service.envs = envs

        when:
        service.initializeEnvironmentConfigurations()

        then:
        Environment.count() == 1
        def env = Environment.first()
        env.logicalName == 'DEV'
        env.tenant == 'tenant1'
        env.level == 1
    }

    void "test initializeApplications"() {
        given:
        // Create a mock environment
        new Environment(logicalName: 'DEV', tenant: 'tenant1', level: 1).save(flush: true)

        def apps = [
                'MyProject': [
                        global   : [
                                repositoryUrl  : 'http://repo.url',
                                gitlabProjectId: 123
                        ],
                        instances: [
                                DEV: [
                                        tenant1: [
                                                baseUrl: 'http://base.url'
                                        ]
                                ]
                        ]
                ]
        ]
        service.apps = apps

        when:
        service.initializeApplications()

        then:
        Project.count() == 1
        ApplicationInstance.count() == 1
        def project = Project.first()
        project.name == 'MyProject'
        project.repositoryUrl == 'http://repo.url'
        project.gitlabProjectId == 123
        def instance = ApplicationInstance.first()
        instance.project.id == project.id // Compare by ID for robustness
        instance.environment.logicalName == 'DEV'
        instance.tenant == 'tenant1'
        instance.baseUrl == 'http://base.url'
    }

    void "test getApplicationStructure"() {
        given:
        def project = new Project(name: 'TestProject', repositoryUrl: 'http://repo.url').save(flush: true)
        def env = new Environment(logicalName: 'PROD', tenant: 'tenant1', level: 3).save(flush: true)
        new ApplicationInstance(project: project, environment: env, tenant: 'tenant1', baseUrl: 'http://app.url').save(flush: true)

        when:
        def structure = service.getApplicationStructure()

        then:
        structure.size() == 1
        structure[env.id].size() == 1
        def appData = structure[env.id][0]
        appData.name == 'TestProject'
        appData.baseUrl == 'http://app.url'
        appData.environment == 'PROD'
    }

    void "test getSummary"() {
        given:
        def statusByEnvironment = [
                'DEV' : [
                        [status: true],
                        [status: false]
                ],
                'PROD': [
                        [status: true],
                        [status: true]
                ]
        ]

        when:
        def summary = service.getSummary(statusByEnvironment)

        then:
        summary.totalDown == 1
        summary.problematicEnvs.size() == 1
        summary.problematicEnvs['DEV'] == 1
    }

    void "test refreshApplicationsStatus"() {
        given:
        def project = new Project(name: 'TestProject', repositoryUrl: 'http://repo.url', gitlabProjectId: 456).save(flush: true)
        def env = new Environment(logicalName: 'PROD', tenant: 'tenant1', level: 3).save(flush: true)
        def instance = new ApplicationInstance(project: project, environment: env, tenant: 'tenant1', healthUrl: 'http://health.url', supervisionUrl: 'http://supervision.url').save(flush: true)

        // Mock service responses
        healthcheckClientService.checkHealth(_) >> true
        supervisionClientService.checkSupervision(_) >> [status: 'OK']
        gitLabClientService.getTestsStatus(_, _) >> [pipelineUrl: 'http://pipeline.url', jobs: []]

        when:
        def statuses = service.refreshApplicationsStatus(env.id)

        then:
        statuses.size() == 1
        def status = statuses[0]
        status.name == 'TestProject'
        status.status == true
        status.supervisionReport.status == 'OK'
        status.pipelineUrl == 'http://pipeline.url'
    }
}
