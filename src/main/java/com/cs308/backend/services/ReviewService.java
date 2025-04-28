package com.cs308.backend.services;


import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;


import org.springframework.stereotype.Service;


import java.util.*;


// in com.cs308.backend.services.ReviewService.java


@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;


    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }


    public Review postReview(Review review) {
        // If user leaves a comment, set it as unverified
        if (review.getComment() != null && !review.getComment().isBlank()) {
            review.setVerified(false);   // comment must be approved
        } else {
            review.setVerified(true);    // no comment = auto-verified
        }


        Review saved = reviewRepository.save(review);


        // 1) Link into product.reviewIds
        Product product = productRepository.findById(saved.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found: " + saved.getProductId()));


        if (product.getReviewIds() == null) {
            product.setReviewIds(new ArrayList<>());
        }
        product.getReviewIds().add(saved.getReviewId());
        productRepository.save(product);


        // 2) Always immediately recompute the rating using ONLY real ratings
        List<Review> allReviews = reviewRepository.findByProductId(product.getProductId());


        List<Review> ratedReviews = allReviews.stream()
                .filter(r -> r.getRating() > 0)
                .toList();


        double avg = ratedReviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);


        product.setRating(avg);
        productRepository.save(product);


        return saved;
    }






    public Review approveReview(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        review.setVerified(true);
        return reviewRepository.save(review);
    }


    public Review declineReview(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        review.setVerified(false);
        return reviewRepository.save(review);
    }


    public List<Review> getVerifiedReviewsForProduct(String productId) {
        List<Review> allVerified = reviewRepository.findByProductIdAndVerifiedTrue(productId);


        return allVerified.stream()
                .filter(r -> {
                    String c = r.getComment();
                    return c != null && !c.trim().isEmpty();
                })
                .toList();
    }


    public void deleteReview(String reviewId) {
        // 1) Fetch the review first
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));


        // 2) Fetch the associated product
        Product product = productRepository.findById(review.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + review.getProductId()));


        // 3) Remove the review ID from the product's reviewIds list
        if (product.getReviewIds() != null) {
            product.getReviewIds().removeIf(id -> id.equals(reviewId));
        }


        // 4) Save the updated product
        productRepository.save(product);


        // 5) Delete the review
        reviewRepository.deleteById(reviewId);


        // 6) Recompute the average rating
        List<Review> remainingReviews = reviewRepository.findByProductId(product.getProductId());
        double newAvg = remainingReviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        product.setRating(newAvg);


        // 7) Save product again after rating update
        productRepository.save(product);
    }


}
