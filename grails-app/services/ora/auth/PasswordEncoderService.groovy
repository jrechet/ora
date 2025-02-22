package ora.auth

import grails.gorm.transactions.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.beans.factory.annotation.Autowired

@Transactional
class PasswordEncoderService {

    @Autowired
    PasswordEncoder passwordEncoder

    String encode(String password) {
        return passwordEncoder.encode(password)
    }
}
