package com.jeudry.rosafiesta.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Cliente HTTP Ktor hacia el servidor Go de RosaFiesta (`/healthz`, `/v1/catalog/articles`, `/v1/events`).
 */
public class RosaFiestaApiClient(
    public val baseUrl: String = "http://localhost:8788",
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    },
) {
    /**
     * Consulta el estado de salud del servidor Go (`/healthz`).
     */
    public suspend fun fetchHealthRaw(): String = httpClient.get("$baseUrl/healthz").bodyAsText()
}
