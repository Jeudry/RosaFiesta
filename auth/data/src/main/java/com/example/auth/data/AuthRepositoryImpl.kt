package com.example.auth.data

import com.example.auth.domain.AuthRepository
import com.example.core.data.networking.post
import com.example.core.domain.AuthInfo
import com.example.core.domain.SessionStorage
import com.example.core.domain.utils.DataError
import com.example.core.domain.utils.EmptyResult
import com.example.core.domain.utils.Result
import com.example.core.domain.utils.asEmptyDataResult
import io.ktor.client.HttpClient

class AuthRepositoryImpl(
  private val httpClient: HttpClient,
  private val sessionStorage: SessionStorage
) : AuthRepository {
  override suspend fun login(email: String, password: String): EmptyResult<DataError.Network> {
    val result = httpClient.post<LoginRequest, LoginResponse>(
      route = "/login",
      body = LoginRequest(
        email = email,
        password = password
      )
    )

    if (result is Result.Success) {
      sessionStorage.set(
        AuthInfo(
          accessToken = result.data.accessToken,
          refreshToken = result.data.refreshToken,
          userId = result.data.userId
        )
      )
    }
    return result.asEmptyDataResult()
  }

  override suspend fun register(email: String, password: String): EmptyResult<DataError.Network> {
    return httpClient.post<RegisterRequest, Unit>(
      route = "/register",
      body = RegisterRequest(email, password)
    )
  }
}