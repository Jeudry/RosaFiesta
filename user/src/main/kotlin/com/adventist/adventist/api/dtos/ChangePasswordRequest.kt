package com.adventist.adventist.api.dtos

import com.adventist.adventist.api.utils.Password
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class ChangePasswordRequest(
    @field:NotBlank
    val oldPassword: String,
    @field:Password
    val newPassword: String,
)
