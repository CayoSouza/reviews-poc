package com.ifood.reviews.review

import java.util.*

data class ReviewEvent(
    val reviewId: UUID,
    val orderId: UUID,
    val userId: UUID,
    val restaurantId: UUID,
    val stars: Int,
    val comment: String?,
    val date: Date
)
