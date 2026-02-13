package com.example.auth.domain

import com.example.core.domain.utils.DataError
import com.example.core.domain.utils.EmptyResult

interface AuthRepository {

  suspend fun login(email: String, password: String): EmptyResult<DataError.Network>
  suspend fun register(email: String, password: String): EmptyResult<DataError.Network>
}