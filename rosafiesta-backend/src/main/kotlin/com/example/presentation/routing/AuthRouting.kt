package com.example.presentation.routing

import com.example.presentation.requests.AuthRequest
import com.example.presentation.requests.RefreshTokenRequest
import com.example.presentation.requests.SignUpRequest
import com.example.presentation.responses.AuthResponse
import com.example.core.services.HashingService
import com.example.data.models.SaltedHash
import com.example.data.models.TokenClaim
import com.example.core.services.JwtService
import com.example.data.services.users.UserService
import com.example.domain.models.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.authRoute() {
    val hashingService by inject<HashingService>()
    val tokenService by inject<JwtService>()
    val userService by inject<UserService>()

    route("/api/auth") {
        post("signup") {
            val request = call.receiveNullable<SignUpRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
            val isPwTooShort = request.password.length < 8
            if (areFieldsBlank || isPwTooShort) {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }
            val saltedHash = hashingService.generateSaltedHash(request.password)
            val user = User(
                userName = request.username,
                salt = saltedHash.salt,
                email = request.email,
                password = saltedHash.hash,
                created = System.currentTimeMillis().toString(),
                avatar = request.avatar,
                phoneNumber = request.phoneNumber,
                bornDate = request.bornDate,
                firstName = request.firstName,
                lastName = request.lastName
            )
            val id = userService.add(user)
            if (id == null) {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }
            call.respond(HttpStatusCode.OK)
        }

        post("signin") {
            val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val invalidCredentialsMessage = "Invalid username or password"

            val authResponse = userService.authenticate(
                request
            )

            if(authResponse == null) {
                call.respond(HttpStatusCode.NotFound, message = invalidCredentialsMessage)
                return@post
            }

            call.respond(HttpStatusCode.OK,
                message = AuthResponse(
                    token = authResponse.token,
                    refreshToken = authResponse.refreshToken
                )
            )
        }

        post("refresh"){
            val request = call.receive<RefreshTokenRequest>()

            val newAccessToken: String? = userService.refreshToken(request.accessToken)

            newAccessToken?.let { accessToken ->
                call.respond(HttpStatusCode.OK, accessToken)
            } ?: call.respond(HttpStatusCode.NotFound)
        }

        authenticate {
            get("secret") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", String::class)
                call.respond(HttpStatusCode.OK, "Your user id is $userId")
            }
        }
    }
}