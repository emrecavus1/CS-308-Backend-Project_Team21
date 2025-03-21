package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
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

    public Review postReview(Review review) {
        // Ensure the review is unverified by default
        review.setVerified(false);

        // Save the review to the database
        Review savedReview = reviewRepository.save(review);

        // Retrieve the product associated with the review
        Product product = productRepository.findById(review.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + review.getProductId()));

        // Initialize the reviewIds list if null, then add the new review's id
        if (product.getReviewIds() == null) {
            product.setReviewIds(new ArrayList<>());
        }
        product.getReviewIds().add(savedReview.getReviewId());

        // Save the updated product
        productRepository.save(product);

        return savedReview;
    }


}
