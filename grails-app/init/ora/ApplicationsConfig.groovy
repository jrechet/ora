package ora

import grails.config.Config
import groovy.util.logging.Slf4j
import org.grails.config.PropertySourcesConfig
import org.grails.config.yaml.YamlPropertySourceLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component

@Slf4j
@Component
@Configuration
class ApplicationsConfig {

    private static final String DEFAULT_CONFIG_PATH = "classpath:ora/"
    private static final String CONFIG_PATH_ENV_VAR = "ORA_CONFIG_PATH"

    @Autowired
    ResourceLoader resourceLoader

    private static String getConfigPath() {
        def configPath = System.getenv(CONFIG_PATH_ENV_VAR)
        if (!configPath) {
            log.debug("No custom config path found in environment variable ${CONFIG_PATH_ENV_VAR}, using default path: ${DEFAULT_CONFIG_PATH}")
            return DEFAULT_CONFIG_PATH
        }

        // Si le chemin ne se termine pas par un slash, on l'ajoute
        if (!configPath.endsWith("/")) {
            configPath += "/"
        }

        // Si le chemin ne commence pas par classpath: ou file:, on ajoute file:
        if (!configPath.startsWith("classpath:") && !configPath.startsWith("file:")) {
            configPath = "file:" + configPath
        }

        log.info("Using custom config path from environment: ${configPath}")
        return configPath
    }

    Config loadApplicationsConfig() {
        def loader = new YamlPropertySourceLoader()
        def resolver = new PathMatchingResourcePatternResolver(resourceLoader)

        try {
            // Créer une map mutable pour fusionner les configurations
            def mergedMap = [:]

            // Construire les chemins de ressources
            def configPath = getConfigPath()
            def envsPath = configPath + "envs.yml"
            def appsPattern = configPath + "apps/*.yml"

            log.debug("Loading environments config from: ${envsPath}")
            log.debug("Loading applications config from pattern: ${appsPattern}")

            // Charger les environnements
            def envsResource = resourceLoader.getResource(envsPath)
            if (!envsResource.exists()) {
                log.warn("Environments configuration file not found at: ${envsPath}")
                throw new IllegalStateException("Required environments configuration file not found")
            }

            def envsProperties = loader.load("envs-config", envsResource).first()
            mergedMap.putAll(envsProperties.getSource())

            // Charger les configurations des applications
            def appResources = resolver.getResources(appsPattern)
            if (!appResources) {
                log.warn("No application configuration files found matching pattern: ${appsPattern}")
            }

            appResources.each { resource ->
                log.debug("Loading configuration from: ${resource.filename}")
                def appProperties = loader.load("app-config-${resource.filename}", resource).first()
                recursiveMerge(mergedMap, appProperties.getSource())
            }

            return new PropertySourcesConfig(new MapPropertySource("merged-config", mergedMap))
        } catch (Exception e) {
            log.error("Error loading configuration", e)
            throw e
        }
    }

    protected void recursiveMerge(Map target, Map source) {
        source.each { k, v ->
            if (v instanceof Map) {
                if (!target.containsKey(k)) {
                    target[k] = [:]
                }
                if (target[k] instanceof Map) {
                    recursiveMerge(target[k], v)
                    return
                }
            }
            target[k] = v
        }
    }
}