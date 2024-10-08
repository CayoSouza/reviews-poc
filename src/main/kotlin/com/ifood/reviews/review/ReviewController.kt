package com.ifood.reviews.review.controller

import com.ifood.reviews.review.model.Review
import com.ifood.reviews.review.model.ReviewDTO
import com.ifood.reviews.review.service.ReviewService
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService
) {

    private val logger = LoggerFactory.getLogger(ReviewController::class.java)

    // Create a new review
    @PostMapping
    suspend fun createReview(@Valid @RequestBody reviewDTO: ReviewDTO): ResponseEntity<Review> {
        logger.info("Received request to create review")
        val savedReview = reviewService.createReview(reviewDTO)
        val location = URI.create("/api/reviews/${savedReview.reviewId}")
        return ResponseEntity.created(location).body(savedReview)
    }

    // Get a review by orderId
    @GetMapping("/order/{orderId}")
    suspend fun getReviewByOrderId(@PathVariable orderId: UUID): ResponseEntity<Review?> {
        logger.info("[PostgreSQL] Received request to get review for orderId: $orderId")
        val review = reviewService.getReviewByOrderId(orderId)
        return if (review != null) {
            ResponseEntity.ok(review)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // Get average stars for a restaurant by restaurantId
    @GetMapping("/restaurant/{restaurantId}/average")
    suspend fun getAverageStars(
        @PathVariable restaurantId: UUID,
        @RequestParam(defaultValue = "true") useNoSql: Boolean
    ): Map<String, Any> {
        logger.info("[NoSQL=$useNoSql] Received request to get average stars for restaurantId: $restaurantId")
        val averageStars = reviewService.calculateAverageStars(restaurantId, useNoSql)
        return mapOf("restaurantId" to restaurantId, "averageStars" to averageStars, "useNoSql" to useNoSql)
    }

    // Get paginated reviews for a restaurant by restaurantId
    @GetMapping("/restaurant/{restaurantId}")
    suspend fun getReviewsByRestaurantId(
        @PathVariable restaurantId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<List<Review>> {
        logger.info("[PostgreSQL] Received request to get paginated reviews for restaurantId: $restaurantId")

        val paginatedReviews = reviewService.getReviewsByRestaurantId(restaurantId, page, size)

        return ResponseEntity.ok(paginatedReviews)
    }

    @GetMapping("/restaurant/{restaurantId}/count")
    suspend fun countReviewsByRestaurantId(
        @PathVariable restaurantId: UUID,
        @RequestParam(defaultValue = "true") useNoSql: Boolean
    ): ResponseEntity<Long> {
        val dataSource = if (useNoSql) "[MongoDB]" else "[PostgreSQL]"
        logger.info("$dataSource Received request to count reviews for restaurantId: $restaurantId")
        val count = reviewService.countReviewsByRestaurantId(restaurantId, useNoSql)
        return ResponseEntity.ok(count)
    }

    @PostMapping("/generate")
    suspend fun generateFakeReviews(
        @RequestParam restaurantId: UUID,
        @RequestParam numberOfReviews: Int
    ): ResponseEntity<Map<String, Any>> = runBlocking {
        reviewService.generateFakeReviews(restaurantId, numberOfReviews)
        val response = mapOf(
            "message" to "Successfully generated $numberOfReviews reviews for restaurant $restaurantId",
            "restaurantId" to restaurantId,
            "numberOfReviews" to numberOfReviews
        )
        ResponseEntity.ok(response)
    }
}
