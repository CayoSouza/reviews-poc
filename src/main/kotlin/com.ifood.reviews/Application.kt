package com.ifood.reviews

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.ifood.reviews"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
