package com.example.core.domain

interface AuthService {
  /**
   * Verifica si el usuario está autenticado y su token es válido
   * @return true si está autenticado con un token válido, false en caso contrario
   */
  suspend fun isUserAuthenticated(): Boolean

  /**
   * Obtiene la información de autenticación si es válida
   * @return AuthInfo si la sesión es válida, null en caso contrario
   */
  suspend fun getValidAuthInfo(): AuthInfo?

  /**
   * Limpia la sesión si el token ha expirado
   */
  suspend fun clearExpiredSession()
}
