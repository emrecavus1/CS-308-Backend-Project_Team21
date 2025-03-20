package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<Review> findByUserId(String userId) {
        return reviewRepository.findByUserId(userId);
    }

    public List<Review> findByIsVerifiedTrue() {
        return reviewRepository.findByIsVerifiedTrue();
    }

    public boolean isVerified(String reviewId) {
        return reviewRepository.findById(reviewId).isPresent();
    }

    public Review approveReview(String reviewId) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setVerified(true); // Approving the review
            return reviewRepository.save(review);
        } else {
            throw new IllegalArgumentException("Review not found with ID: " + reviewId);
        }
    }

}
