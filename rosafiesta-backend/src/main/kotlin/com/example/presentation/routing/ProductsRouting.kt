package com.example.presentation.routing

import com.example.domain.models.Product
import com.example.data.repositories.PostgresProductRepository
import com.example.data.services.products.ProductsService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.productsRoute() {
    val productsService by inject<ProductsService>()

    route("/api/products") {
        get {
            val products = productsService.retrieveAll()
            call.respond(products)
        }

        authenticate {
            get("/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val product = productsService.retrieveById(id = UUID.fromString(id))
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound
                    )
                call.respond(product)
            }

            get("/{name}") {
                val name =
                    call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val product =
                    productsService.retrieveByName(name = name) ?: return@get call.respond(
                        HttpStatusCode.NotFound
                    )
                call.respond(product)
            }

            post {
                try {
                    val product = call.receive<Product>()
                    productsService.addProduct(product)
                    call.respond(HttpStatusCode.Created)
                } catch (ex: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (ex: JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            put("{id}") {
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val product = call.receive<Product>()
                productsService.removeProduct(id = UUID.fromString(id))
                call.respond(HttpStatusCode.OK, product)
            }

            delete("{id}") {
                val id =
                    call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                productsService.removeProduct(id = UUID.fromString(id))
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}