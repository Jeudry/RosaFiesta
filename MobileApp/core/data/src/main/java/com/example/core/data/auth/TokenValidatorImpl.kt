package com.example.core.data.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.example.core.domain.TokenValidator
import timber.log.Timber
import java.util.Date

class TokenValidatorImpl : TokenValidator {

    override fun isTokenValid(token: String): Boolean {
        return try {
            if (token.isBlank()) return false

            // Decodificar el token usando java-jwt
            val decodedJWT = JWT.decode(token)

            // Verificar que no esté expirado
            !isTokenExpired(token)
        } catch (e: JWTDecodeException) {
            Timber.e(e, "Error decoding JWT token")
            false
        } catch (e: Exception) {
            Timber.e(e, "Error validating JWT token")
            false
        }
    }

    override fun getExpirationTime(token: String): Long? {
        return try {
            val decodedJWT = JWT.decode(token)
            decodedJWT.expiresAt?.time
        } catch (e: JWTDecodeException) {
            Timber.e(e, "Error decoding JWT token to get expiration time")
            null
        } catch (e: Exception) {
            Timber.e(e, "Error getting expiration time from JWT token")
            null
        }
    }

    override fun isTokenExpired(token: String): Boolean {
        return try {
            val decodedJWT = JWT.decode(token)
            val expirationTime = decodedJWT.expiresAt

            if (expirationTime == null) {
                Timber.w("JWT token does not have expiration time")
                return false // Si no tiene fecha de expiración, consideramos que no ha expirado
            }

            val currentTime = Date()
            val isExpired = currentTime.after(expirationTime)

            if (isExpired) {
                Timber.d("JWT token has expired. Expiration: $expirationTime, Current: $currentTime")
            }

            isExpired
        } catch (e: JWTDecodeException) {
            Timber.e(e, "Error decoding JWT token to check expiration")
            true // Si hay error, consideramos que está expirado por seguridad
        } catch (e: Exception) {
            Timber.e(e, "Error checking JWT token expiration")
            true // Si hay error, consideramos que está expirado por seguridad
        }
    }
}
