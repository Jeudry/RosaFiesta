package com.example.core.data.auth

import com.example.core.domain.AuthInfo
import com.example.core.domain.AuthService
import com.example.core.domain.SessionStorage
import com.example.core.domain.TokenValidator
import timber.log.Timber

class AuthServiceImpl(
    private val sessionStorage: SessionStorage,
    private val tokenValidator: TokenValidator
) : AuthService {

    override suspend fun isUserAuthenticated(): Boolean {
        val authInfo = sessionStorage.get() ?: return false

        return try {
            val isAccessTokenValid = tokenValidator.isTokenValid(authInfo.accessToken)

            if (!isAccessTokenValid) {
                Timber.d("Access token is invalid or expired")
                clearExpiredSession()
                return false
            }

            true
        } catch (e: Exception) {
            Timber.e(e, "Error checking authentication status")
            clearExpiredSession()
            false
        }
    }

    override suspend fun getValidAuthInfo(): AuthInfo? {
        val authInfo = sessionStorage.get() ?: return null

        return try {
            val isAccessTokenValid = tokenValidator.isTokenValid(authInfo.accessToken)

            if (!isAccessTokenValid) {
                Timber.d("Access token is invalid or expired, clearing session")
                clearExpiredSession()
                return null
            }

            authInfo
        } catch (e: Exception) {
            Timber.e(e, "Error validating auth info")
            clearExpiredSession()
            null
        }
    }

    override suspend fun clearExpiredSession() {
        try {
            sessionStorage.set(null)
            Timber.d("Expired session cleared")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing expired session")
        }
    }
}
