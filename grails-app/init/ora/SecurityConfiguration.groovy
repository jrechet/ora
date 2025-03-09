package ora

import grails.plugin.springsecurity.SpringSecurityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

/**
 * Configuration de sécurité pour l'application.
 * Cette classe configure explicitement Spring Security pour permettre les invocations publiques.
 */
@Configuration
class SecurityConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class)

    /**
     * Initialisation de la configuration de sécurité.
     * Cette méthode est appelée au démarrage de l'application.
     */
    @PostConstruct
    void init() {
        log.info("Initializing security configuration...")
        
        // Configurer Spring Security pour permettre les invocations publiques
        SpringSecurityUtils.securityConfig.rejectIfNoRule = false
        SpringSecurityUtils.securityConfig.rejectPublicInvocations = false
        
        // Ajouter une règle explicite pour l'endpoint WebSocket
        def urlMappings = SpringSecurityUtils.securityConfig.interceptUrlMap ?: []
        urlMappings << [pattern: '/monitoring-ws/**', access: ['permitAll']]
        urlMappings << [pattern: '/monitoring-ws', access: ['permitAll']]
        urlMappings << [pattern: '/monitoring-ws/info', access: ['permitAll']]
        urlMappings << [pattern: '/monitoring-ws/info/**', access: ['permitAll']]
        urlMappings << [pattern: '/monitoring-ws/websocket', access: ['permitAll']]
        urlMappings << [pattern: '/monitoring-ws/websocket/**', access: ['permitAll']]
        
        // Add to filterChain to skip security filters
        def chainMap = SpringSecurityUtils.securityConfig.filterChain.chainMap ?: []
        chainMap << [
            pattern: '/monitoring-ws/**',
            filters: 'none'
        ]
        SpringSecurityUtils.securityConfig.filterChain.chainMap = chainMap
        
        // Set interceptUrlMap
        SpringSecurityUtils.securityConfig.interceptUrlMap = urlMappings
        
        log.info("Security configuration initialized successfully")
    }
}
