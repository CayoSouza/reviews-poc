package com.ifood.reviews.review

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ReviewRepository : JpaRepository<Review, UUID> {

    fun findByOrderId(orderId: UUID): Optional<Review>

    @Query("SELECT r FROM Review r WHERE r.restaurantId = :restaurantId ORDER BY r.date DESC")
    fun findPaginatedByRestaurantId(
        @Param("restaurantId") restaurantId: UUID,
        pageable: Pageable
    ): List<Review>
}