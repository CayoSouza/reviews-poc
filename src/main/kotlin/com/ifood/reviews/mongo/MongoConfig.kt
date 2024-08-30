package com.ifood.reviews.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.UuidRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import java.util.concurrent.TimeUnit

@Configuration
class MongoConfig {

    @Value("\${spring.data.mongodb.uri}")
    private lateinit var mongoUri: String

    @Bean
    fun mongoClient(): MongoClient {
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(mongoUri))
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyToConnectionPoolSettings { builder ->
                builder
                    .maxSize(201) // Set the maximum size of the connection pool
                    .minSize(10) // Set the minimum size of the connection pool
                    .maxWaitTime(30000, TimeUnit.MILLISECONDS) // Set the maximum wait time for a connection
            }
            .build()
        return MongoClients.create(settings)
    }

    @Bean
    fun mongoTemplate(): MongoTemplate {
        return MongoTemplate(SimpleMongoClientDatabaseFactory(mongoClient(), "ifood_reviews"))
    }
}