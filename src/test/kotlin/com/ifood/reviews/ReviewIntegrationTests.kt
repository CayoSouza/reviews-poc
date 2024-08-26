import com.ifood.reviews.Application
import com.ifood.reviews.review.Review
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*

@SpringBootTest(classes = [Application::class, ReviewService::class])
@EmbeddedKafka(partitions = 1)
@Testcontainers
class ReviewIntegrationTests {

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:16.2").apply {
            withDatabaseName("ifood_reviews")
            withUsername("ifood")
            withPassword("password")
        }

        @Container
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // PostgreSQL properties
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }

            // Kafka properties
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
        }
    }

    @Autowired
    lateinit var reviewService: ReviewService

    @MockBean
    lateinit var druidService: DruidService

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
    fun `should calculate average stars correctly using Druid`() = runBlocking {
        val restaurantId = UUID.randomUUID()

        `when`(druidService.getAverageStars(restaurantId)).thenReturn(4.5)

        val averageStars = reviewService.calculateAverageStars(restaurantId)
        assertEquals(4.5, averageStars)
    }
}
