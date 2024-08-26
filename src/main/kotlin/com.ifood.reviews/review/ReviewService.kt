
import com.ifood.reviews.kafka.ReviewKafkaProducer
import com.ifood.reviews.review.Review
import com.ifood.reviews.review.ReviewEvent
import com.ifood.reviews.review.ReviewRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val kafkaProducer: ReviewKafkaProducer,
    private val druidService: DruidService
) {

    private val logger = LoggerFactory.getLogger(ReviewService::class.java)

    suspend fun createReview(review: Review): Review = coroutineScope {
        // Ensure order has not been reviewed before proceeding
        reviewRepository.findByOrderId(review.orderId).ifPresent {
            throw IllegalArgumentException("Order has already been reviewed")
        }

        // Launch both operations in parallel using async
        val saveReviewAsync = async {
            reviewRepository.save(review).also {
                logger.info("Review saved successfully in PostgreSQL with reviewId: ${it.reviewId}")
            }
        }

        val sendKafkaAsync = async {
            try {
                kafkaProducer.sendReview(review.toEvent())
                logger.info("Review sent successfully to Kafka with reviewId: ${review.reviewId}")
            } catch (ex: Exception) {
                logger.error("Failed to send review to Kafka for reviewId: ${review.reviewId}", ex)
                throw ex
            }
        }

        // Await both operations
        val savedReview = saveReviewAsync.await()
        sendKafkaAsync.await()

        // Return saved review after both operations are complete
        savedReview
    }

    fun calculateAverageStars(restaurantId: UUID): Double {
        return druidService.getAverageStars(restaurantId)
    }

    fun getReviewById(reviewId: UUID): Review? {
        return reviewRepository.findById(reviewId).orElse(null)
    }

    private fun Review.toEvent(): ReviewEvent {
        return ReviewEvent(
            reviewId = this.reviewId!!,
            orderId = this.orderId,
            userId = this.userId,
            restaurantId = this.restaurantId,
            stars = this.stars,
            comment = this.comment,
            date = this.date
        )
    }
}
