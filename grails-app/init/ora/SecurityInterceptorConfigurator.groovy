package ora

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor
import org.springframework.stereotype.Component

/**
 * Cette classe intervient après la création de tous les beans Spring
 * pour modifier spécifiquement le FilterSecurityInterceptor et
 * autoriser les invocations publiques.
 */
@Component
class SecurityInterceptorConfigurator implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(SecurityInterceptorConfigurator.class)

    /**
     * Cette méthode est appelée pour chaque bean après son initialisation
     */
    @Override
    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Vérifier si le bean est un FilterSecurityInterceptor
        if (bean instanceof FilterSecurityInterceptor) {
            log.info("Configuration de FilterSecurityInterceptor pour autoriser les invocations publiques...")

            FilterSecurityInterceptor interceptor = (FilterSecurityInterceptor) bean

            // Désactiver le rejet des invocations publiques
            interceptor.setRejectPublicInvocations(false)

            log.info("FilterSecurityInterceptor configuré avec succès: rejectPublicInvocations=false")
        }

        return bean
    }
}