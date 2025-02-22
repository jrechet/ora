package ora

import grails.util.Environment
import ora.auth.Role
import ora.auth.User
import ora.auth.UserRole
import ora.auth.PasswordEncoderService

class BootStrap {

    PasswordEncoderService passwordEncoderService

    def init = { servletContext ->
        if (Environment.current != Environment.PRODUCTION) {
            Role.withTransaction {
                def adminRole = new Role(authority: 'ROLE_ADMIN').save(flush: true)
                def userRole = new Role(authority: 'ROLE_USER').save(flush: true)

                def adminUser = new User(
                        username: 'admin',
                        password: passwordEncoderService.encode('admin123')
                ).save(flush: true)

                def normalUser = new User(
                        username: 'user',
                        password: passwordEncoderService.encode('user123')
                ).save(flush: true)

                UserRole.create(adminUser, adminRole, true)
                UserRole.create(normalUser, userRole, true)
            }
        }
    }

    def destroy = {
    }
}