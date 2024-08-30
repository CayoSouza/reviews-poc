package com.ifood.reviews.review.model

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.util.*

data class ReviewDTO(
    val reviewId: UUID? = null,

    @field:NotNull(message = "OrderId cannot be null")
    val orderId: UUID,

    @field:NotNull(message = "UserId cannot be null")
    val userId: UUID,

    @field:NotNull(message = "RestaurantId cannot be null")
    val restaurantId: UUID,

    @field:Min(1, message = "Stars must be at least 1")
    @field:Max(5, message = "Stars cannot be more than 5")
    val stars: Int,

    val comment: String? = null,

    val date: Date? = null
) {
    fun toEntity(): Review {
        return Review(
            reviewId = this.reviewId,
            orderId = this.orderId,
            userId = this.userId,
            restaurantId = this.restaurantId,
            stars = this.stars,
            comment = this.comment,
            date = this.date ?: Date()
        )
    }
}
