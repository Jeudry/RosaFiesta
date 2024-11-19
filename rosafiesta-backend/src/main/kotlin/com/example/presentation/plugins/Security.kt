package com.example.presentation.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.core.services.HashingService
import com.example.core.services.JwtService
import com.kborowy.authprovider.firebase.firebase
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import java.io.File

fun Application.configureSecurity(
) {
    val jwtService: JwtService by inject<JwtService>()


    /*install(Authentication) {
        firebase {
            adminFile = File("path/to/admin/file.json")
            realm = "My Server"
            validate { token ->
                MyAuthenticatedUser(id = token.uid)
            }
        }
    }*/

    authentication {
        jwt {
            realm = jwtService.jwtConfig.realm
            verifier(jwtService.jwtVerifier)
            validate { credential ->
                if (credential.payload.audience.contains(jwtService.jwtConfig.audience)) JWTPrincipal(credential.payload) else null
            }
        }

        jwt("another-auth") {
            realm = jwtService.jwtConfig.realm
            verifier(jwtService.jwtVerifier)
            validate { credential ->
                if (credential.payload.audience.contains(jwtService.jwtConfig.audience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}