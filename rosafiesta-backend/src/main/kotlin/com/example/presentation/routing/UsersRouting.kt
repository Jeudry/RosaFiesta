package com.example.presentation.routing

import com.example.presentation.requests.UserRequest
import com.example.presentation.requests.toModel
import com.example.core.services.UserServiceImpl
import com.example.data.services.users.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.userRoute(){

    val userService: UserService by inject()
    route("/api/users") {
        post {
            val userRequest = call.receive<UserRequest>()

            val createdUserId = userService.add(
                user = userRequest.toModel()
            )

            if(createdUserId == null) return@post call.respond(HttpStatusCode.Conflict)

            call.response.header(
                name = "id",
                value = createdUserId.toString()
            )

            call.respond(HttpStatusCode.Created, createdUserId)
        }

        authenticate {
            get("/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val user =
                    userService.retrieveById(id = UUID.fromString(id)) ?: return@get call.respond(
                        HttpStatusCode.NotFound
                    )
                call.respond(user)
            }

            get {
                val users = userService.retrieveAll()
                call.respond(users)
            }
        }
    }
}