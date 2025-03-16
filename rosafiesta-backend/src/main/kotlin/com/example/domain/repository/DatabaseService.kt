package com.example.domain.repository

interface DatabaseService {
    suspend fun initialize()
    suspend fun <T> dbQuery(block: () -> T): T
}