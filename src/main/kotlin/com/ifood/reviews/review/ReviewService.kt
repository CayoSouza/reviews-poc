package com.ifood.reviews.review.service

import com.ifood.reviews.review.model.MongoReview
import com.ifood.reviews.review.model.Review
import com.ifood.reviews.review.model.ReviewDTO
import com.ifood.reviews.review.repository.PostgresReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
class ReviewService(
    private val postgresReviewRepository: PostgresReviewRepository,
    private val mongoTemplate: MongoTemplate
) {

    private val logger = LoggerFactory.getLogger(ReviewService::class.java)

    @Transactional
    suspend fun createReview(reviewDTO: ReviewDTO): Review = withContext(Dispatchers.IO) {
        try {
            postgresReviewRepository.findByOrderId(reviewDTO.orderId).ifPresent {
                throw IllegalArgumentException("Order has already been reviewed")
            }

            val review = reviewDTO.toEntity()
            val savedReview = postgresReviewRepository.save(review).also {
                logger.info("Review saved successfully in PostgreSQL with reviewId: ${it.reviewId}")
            }

            val mongoReview = MongoReview(
                reviewId = savedReview.reviewId?.toString(),
                orderId = review.orderId.toString(),
                userId = review.userId.toString(),
                restaurantId = review.restaurantId.toString(),
                stars = savedReview.stars,
                comment = savedReview.comment,
                date = savedReview.date
            )

            mongoTemplate.save(mongoReview).also {
                logger.info("Review saved successfully in MongoDB with reviewId: ${it.reviewId}")
            }

            return@withContext savedReview
        } catch (e: Exception) {
            logger.error("Error creating review: ${e.message}", e)
            throw e
        }
    }

    suspend fun calculateAverageStars(restaurantId: UUID, useNoSql: Boolean): Double = withContext(Dispatchers.IO) {
        if (useNoSql) calculateAverageStarsMongo(restaurantId)
        else calculateAverageStarsPostgres(restaurantId)
    }

    private fun calculateAverageStarsMongo(restaurantId: UUID): Double {
        logger.info("Calculating average in MongoDB for restaurantId: $restaurantId")

        val matchOperation = match(Criteria.where("restaurantId").`is`(restaurantId.toString()))
        val groupOperation = group().avg("stars").`as`("averageStars")
        val aggregation = newAggregation(matchOperation, groupOperation)

        val result = mongoTemplate.aggregate(aggregation, MongoReview::class.java, AverageStarsResult::class.java)
        return result.mappedResults.firstOrNull()?.averageStars ?: 0.0
    }

    private fun calculateAverageStarsPostgres(restaurantId: UUID): Double {
        logger.info("Calculating average in PostgreSQL for restaurantId: $restaurantId")
        return postgresReviewRepository.calculateAverageStarsPostgres(restaurantId) ?: 0.0
    }

    suspend fun getReviewByOrderId(orderId: UUID): Review? = withContext(Dispatchers.IO) {
        postgresReviewRepository.findByOrderId(orderId).orElse(null)
    }

    suspend fun getReviewsByRestaurantId(restaurantId: UUID, page: Int, size: Int): List<Review> = withContext(Dispatchers.IO) {
        val pageable = PageRequest.of(page, size)
        postgresReviewRepository.findPaginatedByRestaurantId(restaurantId, pageable)
    }

    suspend fun countReviewsByRestaurantId(restaurantId: UUID, useNoSql: Boolean): Long = withContext(Dispatchers.IO) {
        if (useNoSql) countReviewsByRestaurantIdMongo(restaurantId)
        else countReviewsByRestaurantIdPostgres(restaurantId)
    }

    private fun countReviewsByRestaurantIdMongo(restaurantId: UUID): Long {
        val query = Query(Criteria.where("restaurantId").`is`(restaurantId.toString()))
        return mongoTemplate.count(query, MongoReview::class.java)
    }

    private fun countReviewsByRestaurantIdPostgres(restaurantId: UUID): Long {
        return postgresReviewRepository.countReviewsByRestaurantId(restaurantId)
    }

    data class AverageStarsResult(val averageStars: Double)

    @Transactional
    fun generateFakeReviews(restaurantId: UUID, numberOfReviews: Int) {
        runBlocking {
            val batchSize = 500 // Adjust batch size as needed
            val batches = numberOfReviews / batchSize

            // Parallel processing using coroutines
            val jobs = List(batches) { batch ->
                launch(Dispatchers.IO) {
                    val reviews = mutableListOf<Review>()
                    val mongoReviews = mutableListOf<MongoReview>()
                    repeat(batchSize) {
                        val review = Review(
                            orderId = UUID.randomUUID(),
                            userId = UUID.randomUUID(),
                            restaurantId = restaurantId,
                            stars = (1..5).random(),
                            comment = "Random review",
                            date = Date()
                        )
                        reviews.add(review)

                        val mongoReview = MongoReview(
                            reviewId = review.reviewId.toString(),
                            orderId = review.orderId.toString(),
                            userId = review.userId.toString(),
                            restaurantId = review.restaurantId.toString(),
                            stars = review.stars,
                            comment = review.comment,
                            date = review.date
                        )
                        mongoReviews.add(mongoReview)
                    }

                    // Batch insert into PostgreSQL
                    postgresReviewRepository.saveAll(reviews)

                    // Batch insert into MongoDB
                    mongoTemplate.insertAll(mongoReviews)
                }
            }
            jobs.forEach { it.join() } // Wait for all coroutines to complete
        }
    }


}
