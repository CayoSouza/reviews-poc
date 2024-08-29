package com.ifood.reviews.review

import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ReviewMongoRepository(private val mongoTemplate: MongoTemplate) {

    fun save(review: Review) {
        mongoTemplate.save(review)
    }

    fun calculateAverageStars(restaurantId: UUID): Double {
        val matchOperation = match(org.springframework.data.mongodb.core.query.Criteria.where("restaurantId").`is`(restaurantId))
        val groupOperation = group().avg("stars").`as`("averageStars")
        val aggregation = newAggregation(matchOperation, groupOperation)
        
        val result = mongoTemplate.aggregate(aggregation, "reviews", Document::class.java)
        val doc = result.mappedResults.firstOrNull()
        
        return doc?.getDouble("averageStars") ?: 0.0
    }
}
