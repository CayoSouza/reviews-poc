package com.ifood.reviews.migrations
import com.ifood.reviews.review.Review
import com.ifood.reviews.review.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class ReviewDataGenerator(
    private val reviewRepository: ReviewRepository,
    private val mongoTemplate: MongoTemplate
) {

    @Transactional
    fun generateRandomReviews(restaurantId: UUID, numberOfReviews: Int) {
        runBlocking {
            val batchSize = 1000 // Adjust batch size as needed
            val batches = numberOfReviews / batchSize

            // Parallel processing using coroutines
            val jobs = List(batches) { batch ->
                launch(Dispatchers.IO) {
                    val reviews = mutableListOf<Review>()
                    repeat(batchSize) {
                        val review = Review(
                            orderId = UUID.randomUUID(),
                            userId = UUID.randomUUID(),
                            restaurantId = restaurantId,
                            stars = (1..5).random(),
                            comment = "Random review",
                            date = Date()
                        )
                        reviews.add(review)
                    }

                    // Batch insert into PostgreSQL
                    reviewRepository.saveAll(reviews)

                    // Batch insert into MongoDB
                    mongoTemplate.insertAll(reviews)
                }
            }
            jobs.forEach { it.join() } // Wait for all coroutines to complete
        }
    }
}
