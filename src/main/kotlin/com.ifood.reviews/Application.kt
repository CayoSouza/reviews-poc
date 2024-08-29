package com.ifood.reviews

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.ifood.reviews"])
@EnableJpaRepositories(basePackages = ["com.ifood.reviews.review"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
