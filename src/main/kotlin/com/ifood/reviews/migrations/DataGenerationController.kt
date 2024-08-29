package com.ifood.reviews.migrations

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class DataGenerationController(
    private val reviewDataGenerator: ReviewDataGenerator
) {

    private val logger = LoggerFactory.getLogger(DataGenerationController::class.java)

    @PostMapping("/api/reviews/generate")
    fun generateRandomReviews(
        @RequestParam restaurantId: UUID,
        @RequestParam numberOfReviews: Int
    ): ResponseEntity<String> {
        logger.info("Generating $numberOfReviews reviews for restaurantId: $restaurantId")
        reviewDataGenerator.generateRandomReviews(restaurantId, numberOfReviews)
        return ResponseEntity.ok("Generated $numberOfReviews reviews for restaurantId: $restaurantId")
    }
}
