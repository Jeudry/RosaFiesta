package com.example.data.models

data class TokenConfig(
    val issuer: String,
    val audience: String,
    val domain: String,
    val secret: String,
    val realm: String
)