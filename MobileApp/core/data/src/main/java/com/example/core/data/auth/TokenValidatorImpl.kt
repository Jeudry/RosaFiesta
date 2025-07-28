package com.example.core.data.auth

import com.auth0.android.jwt.JWT
import com.example.core.domain.TokenValidator
import timber.log.Timber
import java.util.Date

class TokenValidatorImpl : TokenValidator {

    override fun isTokenValid(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            !isTokenExpired(token)
        } catch (e: Exception) {
            Timber.e(e, "Error validating JWT token")
            false
        }
    }

    override fun getExpirationTime(token: String): Long? {
        return try {
            val jwt = JWT(token)
            jwt.expiresAt?.time
        } catch (e: Exception) {
            Timber.e(e, "Error getting expiration time from JWT token")
            null
        }
    }

    override fun isTokenExpired(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            val expirationTime = jwt.expiresAt

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
        } catch (e: Exception) {
            Timber.e(e, "Error checking JWT token expiration")
            true // Si hay error, consideramos que está expirado por seguridad
        }
    }
}
