package com.ifood.reviews

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.index.IndexOperations
import org.springframework.stereotype.Component

@SpringBootApplication(scanBasePackages = ["com.ifood.reviews"])
@EnableJpaRepositories(basePackages = ["com.ifood.reviews.review"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Component
class MongoIndexCreator(private val mongoTemplate: MongoTemplate) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        createIndex()
    }

    private fun createIndex() {
        val indexOps: IndexOperations = mongoTemplate.indexOps("reviews")
        val index = Index().on("restaurantId", Sort.Direction.ASC)
        indexOps.ensureIndex(index)
        println("Index created on restaurantId field")
    }
}