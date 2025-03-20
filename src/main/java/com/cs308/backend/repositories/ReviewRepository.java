package com.cs308.backend.repositories;

import com.cs308.backend.models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    // Find reviews by product ID
    List<Review> findByProductId(String productId);

    // Find all reviews by a user
    List<Review> findByUserId(String userId);

    // Find all verified reviews
    List<Review> findByIsVerifiedTrue();
}
