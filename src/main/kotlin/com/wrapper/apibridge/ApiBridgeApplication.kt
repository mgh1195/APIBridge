package com.wrapper.apibridge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApiBridgeApplication

fun main(args: Array<String>) {
    runApplication<ApiBridgeApplication>(*args)
}
