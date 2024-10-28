package com.wrapper.apibridge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
class ApiBridgeApplication

fun main(args: Array<String>) {
    runApplication<ApiBridgeApplication>(*args)
}
