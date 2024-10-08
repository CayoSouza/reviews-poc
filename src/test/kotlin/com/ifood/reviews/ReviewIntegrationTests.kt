
import com.ifood.reviews.Application
import com.ifood.reviews.review.model.ReviewDTO
import com.ifood.reviews.review.service.ReviewService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*

@SpringBootTest(classes = [Application::class, ReviewService::class])
@Testcontainers
class ReviewIntegrationTests {

    companion object {
        // PostgreSQL container
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:16.2").apply {
            withDatabaseName("ifood_reviews")
            withUsername("ifood")
            withPassword("password")
        }

        // MongoDB container
        @Container
        val mongo = MongoDBContainer(DockerImageName.parse("mongo:6.0")).apply {
            withExposedPorts(27017)
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // PostgreSQL properties
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }

            // MongoDB properties
            registry.add("spring.data.mongodb.uri") { mongo.replicaSetUrl }
        }
    }

    @Autowired
    lateinit var reviewService: ReviewService

    @Test
    fun `should create and retrieve a review`() = runBlocking {
        val reviewDTO = ReviewDTO(
            orderId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            restaurantId = UUID.randomUUID(),
            stars = 5,
            comment = "Great food!"
        )

        // Save review in PostgreSQL and MongoDB
        val savedReview = reviewService.createReview(reviewDTO)
        assertNotNull(savedReview)

        // Retrieve the review from PostgreSQL
        val retrievedReview = reviewService.getReviewByOrderId(savedReview.orderId)
        assertEquals(savedReview, retrievedReview)
    }

    @Test
    fun `should calculate average stars correctly from MongoDB with 10 reviews`() = runBlocking {
        val restaurantId = UUID.randomUUID()

        // Create 10 reviews with varying stars for the same restaurant
        val stars = listOf(5, 4, 3, 5, 4, 3, 2, 1, 5, 4)
        stars.forEachIndexed { index, star ->
            val reviewDTO = ReviewDTO(
                orderId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                restaurantId = restaurantId,
                stars = star,
                comment = "Review ${index + 1}"
            )
            reviewService.createReview(reviewDTO)
        }

        // Calculate the average stars from MongoDB
        val expectedAverage = stars.average()
        val averageStars = reviewService.calculateAverageStars(restaurantId, useNoSql = true)

        assertEquals(expectedAverage, averageStars)
    }

    @Test
    fun `should get review by orderId`() = runBlocking {
        val reviewDTO = ReviewDTO(
            orderId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            restaurantId = UUID.randomUUID(),
            stars = 5,
            comment = "Amazing service!"
        )

        // Save the review
        val savedReview = reviewService.createReview(reviewDTO)
        assertNotNull(savedReview)

        // Retrieve the review by orderId
        val retrievedReview = reviewService.getReviewByOrderId(savedReview.orderId)
        assertNotNull(retrievedReview)
        assertEquals(savedReview, retrievedReview)
    }

    @Test
    fun `should calculate average stars for a restaurant with no reviews`() = runBlocking {
        val restaurantId = UUID.randomUUID()

        // Calculate the average stars for a restaurant with no reviews
        val averageStars = reviewService.calculateAverageStars(restaurantId, useNoSql = true)

        // Expect the average to be 0.0 when there are no reviews
        assertEquals(0.0, averageStars)
    }
}
