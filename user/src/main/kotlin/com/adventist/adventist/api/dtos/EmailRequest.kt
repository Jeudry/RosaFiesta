package com.adventist.adventist.api.dtos

import com.adventist.adventist.api.utils.Password
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class EmailRequest(
    @field:Email val email: String,
)
