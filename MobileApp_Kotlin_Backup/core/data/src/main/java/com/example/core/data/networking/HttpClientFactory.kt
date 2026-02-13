package com.example.core.data.networking

import com.example.core.data.BuildConfig
import com.example.core.domain.AuthInfo
import com.example.core.domain.SessionStorage
import com.example.core.domain.utils.Result
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import timber.log.Timber

class HttpClientFactory(
  private val sessionStorage: SessionStorage
) {
  @OptIn(InternalSerializationApi::class)
  fun build(): HttpClient {
    return HttpClient(CIO) {
      install(ContentNegotiation) {
        json(
          json = Json {
            ignoreUnknownKeys = true
          }
        )
      }
      install(Logging) {
        logger = object : Logger {
          override fun log(message: String) {
            Timber.d(message)
          }
        }
      }
      defaultRequest {
        contentType(ContentType.Application.Json)
        header("x-api-key", BuildConfig.API_KEY)
      }
      install(Auth) {
        bearer {
          loadTokens {
            val info = sessionStorage.get()
            if (info?.accessToken.isNullOrEmpty()) {
              null
            } else {
              BearerTokens(
                accessToken = info?.accessToken ?: "",
                refreshToken = info?.refreshToken ?: ""
              )
            }
          }
          refreshTokens {
            val info = sessionStorage.get()
            val response = client.post<AccessTokenRequest, AccessTokenResponse>(
              route = "/v1/authentication/refresh",
              body = AccessTokenRequest(
                refreshToken = info?.refreshToken ?: "",
                userId = info?.userId ?: ""
              )
            )
            when (response) {
              is Result.Success -> {
                val newAuthInfo = AuthInfo(
                  accessToken = response.data.accessToken,
                  refreshToken = info?.refreshToken ?: "",
                  userId = info?.userId ?: ""
                )
                sessionStorage.set(newAuthInfo)

                BearerTokens(
                  accessToken = newAuthInfo.accessToken,
                  refreshToken = newAuthInfo.refreshToken
                )
              }
              is Result.Error -> {
                // En caso de error, limpiar la sesión
                sessionStorage.set(null)
                null
              }
            }
          }
          sendWithoutRequest { request ->
            // Solo enviar el token para endpoints que lo requieran (no para login/register)
            !request.url.pathSegments.contains("authentication")
          }
        }
      }
    }
  }
}
