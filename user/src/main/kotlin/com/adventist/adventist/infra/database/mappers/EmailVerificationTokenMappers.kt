package com.adventist.adventist.infra.database.mappers

import com.adventist.adventist.domain.model.EmailVerificationToken
import com.adventist.adventist.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toModel(): EmailVerificationToken {
    return EmailVerificationToken(
        id = id!!,
        token = token,
        user = user.toModel()
    )
}