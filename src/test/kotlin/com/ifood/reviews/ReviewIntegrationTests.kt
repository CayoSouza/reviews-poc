import com.ifood.reviews.Application
import com.ifood.reviews.review.Review
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
        val mongo = MongoDBContainer(DockerImageName.parse("mongo:5.0")).apply {
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
        val review = Review(
            orderId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            restaurantId = UUID.randomUUID(),
            stars = 5,
            comment = "Great food!"
        )

        val savedReview = reviewService.createReview(review)
        assertNotNull(savedReview)

        val retrievedReview = reviewService.getReviewById(savedReview.reviewId!!)
        assertEquals(savedReview, retrievedReview)
    }

    @Test
    fun `should calculate average stars correctly from MongoDB with 10 reviews`() = runBlocking {
        val restaurantId = UUID.randomUUID()

        // Create 10 reviews with varying stars for the same restaurant
        val stars = listOf(5, 4, 3, 5, 4, 3, 2, 1, 5, 4)
        stars.forEachIndexed { index, star ->
            val review = Review(
                orderId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                restaurantId = restaurantId,
                stars = star,
                comment = "Review ${index + 1}"
            )
            reviewService.createReview(review)
        }

        // Calculate the average stars from MongoDB
        val expectedAverage = stars.average()
        val averageStars = reviewService.calculateAverageStars(restaurantId)

        assertEquals(expectedAverage, averageStars)
    }
}
