package com.ifood.reviews.review.model

import jakarta.persistence.*
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Entity
@Table(name = "reviews")
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // or GenerationType.IDENTITY
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
    // No-argument constructor required by JPA
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


@Document(collection = "reviews")
data class MongoReview(
    @Id
    val reviewId: String? = null,

    val orderId: String,
    val userId: String,

    @Indexed
    val restaurantId: String,
    val stars: Int,
    val comment: String? = null,
    val date: Date = Date()
)

