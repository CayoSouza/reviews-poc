package com.ifood.reviews.controller

import AggregationResponse
import DruidService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/restaurants")
class RestaurantController(
    private val druidService: DruidService
) {

    @GetMapping("/{restaurantId}/aggregations")
    fun getAggregations(@PathVariable restaurantId: UUID): AggregationResponse {
        val averageStars = druidService.getAverageStars(restaurantId)
        return AggregationResponse(averageStars = averageStars)
    }
}
