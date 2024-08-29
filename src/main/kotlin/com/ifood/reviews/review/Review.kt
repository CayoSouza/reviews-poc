package com.ifood.reviews.review

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "reviews")
data class Review(
    @Id
    @GeneratedValue
    val reviewId: UUID? = null,

    @Column(nullable = false)
    val orderId: UUID,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val restaurantId: UUID,

    @Column(nullable = false)
    val stars: Int,

    @Column
    val comment: String? = null,

    @Column(nullable = false)
    val date: Date = Date()
) {
    // Construtor sem argumentos para JPA
    protected constructor() : this(
        reviewId = null,
        orderId = UUID.randomUUID(),
        userId = UUID.randomUUID(),
        restaurantId = UUID.randomUUID(),
        stars = 0,
        comment = null,
        date = Date()
    )
}
