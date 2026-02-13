package com.example.core.domain

interface TokenValidator {
  /**
   * Valida si el token JWT es válido y no ha expirado
   * @param token El token JWT a validar
   * @return true si el token es válido y no ha expirado, false en caso contrario
   */
  fun isTokenValid(token: String): Boolean

  /**
   * Obtiene el tiempo de expiración del token en milisegundos
   * @param token El token JWT
   * @return El timestamp de expiración o null si no se puede obtener
   */
  fun getExpirationTime(token: String): Long?

  /**
   * Verifica si el token ha expirado
   * @param token El token JWT
   * @return true si el token ha expirado, false en caso contrario
   */
  fun isTokenExpired(token: String): Boolean
}
