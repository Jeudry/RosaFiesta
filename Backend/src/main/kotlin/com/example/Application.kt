package com.example

import com.example.data.repositories.PostgresProductRepository
import com.example.presentation.plugins.configureDatabases
import com.example.presentation.plugins.configureFrameworks
import com.example.presentation.plugins.configureHTTP
import com.example.presentation.plugins.configureMonitoring
import com.example.presentation.plugins.configureRouting
import com.example.presentation.plugins.configureSecurity
import com.example.presentation.plugins.configureSerialization
import com.example.presentation.plugins.configureSockets
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val repository = PostgresProductRepository()

    configureSockets()
    configureFrameworks()
    configureSerialization()
    configureDatabases()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureRouting(repository)
}