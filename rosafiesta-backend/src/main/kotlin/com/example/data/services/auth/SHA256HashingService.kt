package com.example.data.services.auth

import org.apache.commons.codec.digest.DigestUtils
import com.example.core.services.HashingService
import com.example.data.models.SaltedHash
import org.apache.commons.codec.binary.Hex
import java.security.SecureRandom

class SHA256HashingService: HashingService {
    override suspend fun generateSaltedHash(value: String, saltLength: Int): SaltedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG")
            .generateSeed(saltLength)
        val saltAsHex = Hex.encodeHexString(salt)
        val hash = DigestUtils.sha256Hex("$salt$value")
        return SaltedHash(
            hash = hash,
            salt = saltAsHex
        )
    }

    override suspend fun verify(value: String, saltedHash: SaltedHash): Boolean {
        return DigestUtils.sha256Hex(saltedHash.salt + value) == saltedHash.salt
    }
}