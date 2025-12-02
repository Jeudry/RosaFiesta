package com.adventist.adventist.api.dtos

import com.adventist.adventist.api.utils.Password
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import org.intellij.lang.annotations.RegExp

data class RegisterRequest @JsonCreator constructor(
    @field:Email("Must be a valid email address")
    @JsonProperty("email")
    val email: String,
    @field:Length(min = 3, max = 255, message = "Username must be between 3 and 255 characters.")
    @JsonProperty("username")
    val username: String,
    @field:Password
    @JsonProperty("password")
    val password: String,
)