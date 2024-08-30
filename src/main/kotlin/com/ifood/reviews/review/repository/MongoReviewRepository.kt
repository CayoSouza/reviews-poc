package com.ifood.reviews.review.repository

import com.ifood.reviews.review.model.MongoReview
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface MongoReviewRepository : MongoRepository<MongoReview, UUID>
