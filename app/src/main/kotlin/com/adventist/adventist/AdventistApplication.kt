package com.adventist.adventist

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class AdventistApplication

fun main(args: Array<String>) {
	runApplication<AdventistApplication>(*args)
}