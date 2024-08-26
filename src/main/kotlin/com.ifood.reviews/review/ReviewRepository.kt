package com.ifood.reviews.review

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ReviewRepository : JpaRepository<Review, UUID> {
    fun findByOrderId(orderId: UUID): Optional<Review>
    fun findByRestaurantId(restaurantId: UUID): List<Review>
}
