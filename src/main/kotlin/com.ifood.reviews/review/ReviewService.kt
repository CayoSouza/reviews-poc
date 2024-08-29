package com.ifood.reviews.review

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val mongoTemplate: MongoTemplate
) {

    private val logger = LoggerFactory.getLogger(ReviewService::class.java)

    @Transactional
    suspend fun createReview(review: Review): Review = withContext(Dispatchers.IO) {
        reviewRepository.findByOrderId(review.orderId).ifPresent {
            throw IllegalArgumentException("Order has already been reviewed")
        }

        val savedReview = reviewRepository.save(review).also {
            logger.info("Review saved successfully in PostgreSQL with reviewId: ${it.reviewId}")
        }

        mongoTemplate.save(savedReview).also {
            logger.info("Review saved successfully in MongoDB with reviewId: ${it.reviewId}")
        }

        savedReview
    }

    suspend fun calculateAverageStars(restaurantId: UUID): Double = withContext(Dispatchers.IO) {
        val query = org.springframework.data.mongodb.core.query.Query(org.springframework.data.mongodb.core.query.Criteria.where("restaurantId").`is`(restaurantId))
        val reviews = mongoTemplate.find(query, Review::class.java)
        val totalStars = reviews.sumOf { it.stars }
        if (reviews.isNotEmpty()) totalStars.toDouble() / reviews.size else 0.0
    }

    suspend fun getReviewById(reviewId: UUID): Review? = withContext(Dispatchers.IO) {
        reviewRepository.findById(reviewId).orElse(null)
    }

    suspend fun getReviewByOrderId(orderId: UUID): Review? {
        return withContext(Dispatchers.IO) {
            reviewRepository.findByOrderId(orderId)
        }.orElse(null)
    }

    suspend fun getReviewsByRestaurantId(restaurantId: UUID, page: Int, size: Int): List<Review> = withContext(Dispatchers.IO) {
        val pageable = PageRequest.of(page, size)
        reviewRepository.findPaginatedByRestaurantId(restaurantId, pageable)
    }

    fun countReviewsByRestaurantId(restaurantId: UUID): Long {
        val query = Query(Criteria.where("restaurantId").`is`(restaurantId))
        return mongoTemplate.count(query, Review::class.java)
    }
}