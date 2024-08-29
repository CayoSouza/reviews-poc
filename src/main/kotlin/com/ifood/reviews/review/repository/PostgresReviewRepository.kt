package com.ifood.reviews.review.repository

import com.ifood.reviews.review.model.Review
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface PostgresReviewRepository : JpaRepository<Review, UUID> {
    fun findByOrderId(orderId: UUID): Optional<Review>

    @Query("SELECT AVG(r.stars) FROM Review r WHERE r.restaurantId = :restaurantId")
    fun calculateAverageStarsPostgres(restaurantId: UUID): Double?

    fun findPaginatedByRestaurantId(restaurantId: UUID, pageable: Pageable): List<Review>

    fun countReviewsByRestaurantId(restaurantId: UUID): Long
}
