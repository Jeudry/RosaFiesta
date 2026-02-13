package com.example.auth.data

import com.example.auth.domain.AuthRepository
import com.example.core.data.models.Envelope
import com.example.core.data.networking.post
import com.example.core.domain.AuthInfo
import com.example.core.domain.SessionStorage
import com.example.core.domain.utils.*
import io.ktor.client.*
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
class AuthRepositoryImpl(
  private val httpClient: HttpClient,
  private val sessionStorage: SessionStorage
) : AuthRepository {

  private companion object {
    private const val BASE_URL = "/v1/authentication"
  }

  override suspend fun login(email: String, password: String): EmptyResult<DataError.Network> {
    val result: Result<Envelope<LoginResponse>, DataError.Network> = httpClient.post<LoginRequest, Envelope<LoginResponse>>(
      route = "$BASE_URL/token",
      body = LoginRequest(
        email = email,
        password = password
      )
    )

    result.onSuccess { resultData ->
      sessionStorage.set(
        AuthInfo(
          accessToken = resultData.data.accessToken,
          refreshToken = resultData.data.refreshToken,
          userId = resultData.data.userId
        )
      )
    }

    return result.asEmptyDataResult()
  }

  override suspend fun register(email: String, password: String): EmptyResult<DataError.Network> {
    return httpClient.post<RegisterRequest, Unit>(
      route = "$BASE_URL/register",
      body = RegisterRequest(email, password)
    )
  }
}