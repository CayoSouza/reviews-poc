package com.ifood.reviews.kafka

import com.ifood.reviews.review.ReviewEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class ReviewKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, ReviewEvent>
) {
    fun sendReview(reviewEvent: ReviewEvent) {
        kafkaTemplate.send("reviews-topic", reviewEvent)
    }
}
