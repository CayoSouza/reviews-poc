package com.ifood.reviews.review

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService
) {

    private val logger = LoggerFactory.getLogger(ReviewController::class.java)

    // Create a new review
    @PostMapping
    suspend fun createReview(@Valid @RequestBody review: Review): ResponseEntity<Review> {
        logger.info("Received request to create review")
        val savedReview = reviewService.createReview(review)
        return ResponseEntity.ok(savedReview)
    }

    // Get a review by orderId
    @GetMapping("/order/{orderId}")
    suspend fun getReviewByOrderId(@PathVariable orderId: UUID): ResponseEntity<Review?> {
        logger.info("[PostgresSQL] Received request to get review for orderId: $orderId")
        val review = reviewService.getReviewByOrderId(orderId)
        return if (review != null) {
            ResponseEntity.ok(review)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // Get average stars for a restaurant by restaurantId
    @GetMapping("/restaurant/{restaurantId}/average-stars")
    suspend fun getAverageStarsByRestaurant(@PathVariable restaurantId: UUID): ResponseEntity<Double> {
        logger.info("[MongoDB] Received request to get average stars for restaurantId: $restaurantId")
        val averageStars = reviewService.calculateAverageStars(restaurantId)
        return ResponseEntity.ok(averageStars)
    }
}
