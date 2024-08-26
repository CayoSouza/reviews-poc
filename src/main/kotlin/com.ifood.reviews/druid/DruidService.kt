import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

@Service
class DruidService(private val restTemplate: RestTemplate) {

    fun getAverageStars(restaurantId: UUID): Double {
        val url = "http://druid-service/aggregations/$restaurantId" // Ajuste a URL conforme necessário
        val response = restTemplate.getForObject(url, AggregationResponse::class.java)
        return response?.averageStars ?: 0.0
    }
}

data class AggregationResponse(
    val averageStars: Double
)
