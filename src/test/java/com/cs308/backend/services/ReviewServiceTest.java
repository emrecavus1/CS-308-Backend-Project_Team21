// 8. ReviewServiceTest.java
package com.cs308.backend.services;

import com.cs308.backend.models.Product;
import com.cs308.backend.models.Review;
import com.cs308.backend.repositories.CartRepository;
import com.cs308.backend.repositories.OrderRepository;
import com.cs308.backend.repositories.ProductRepository;
import com.cs308.backend.repositories.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review testReview;
    private Product testProduct;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID().toString());
        testProduct.setProductName("Test Product");
        testProduct.setReviewIds(new ArrayList<>());

        testReview = new Review();
        testReview.setReviewId(UUID.randomUUID().toString());
        testReview.setUserId(UUID.randomUUID().toString());
        testReview.setProductId(testProduct.getProductId());
        testReview.setRating(4.5);
        testReview.setComment("Great product!");
        testReview.setVerified(false);
    }

    @Test
    public void testPostReview_Successful() {
        // Arrange
        when(productRepository.findById(testReview.getProductId())).thenReturn(Optional.of(testProduct));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Review result = reviewService.postReview(testReview);

        // Assert
        assertFalse(result.isVerified()); // Should be unverified by default
        verify(reviewRepository, times(1)).save(testReview);
        verify(productRepository, times(1)).save(testProduct);
        assertTrue(testProduct.getReviewIds().contains(testReview.getReviewId()));
    }

    @Test
    public void testApproveReview_Successful() {
        // Arrange
        when(reviewRepository.findById(testReview.getReviewId())).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // Act
        Review result = reviewService.approveReview(testReview.getReviewId());

        // Assert
        assertTrue(result.isVerified());
        verify(reviewRepository, times(1)).save(testReview);
    }

    @Test
    public void testApproveReview_ReviewNotFound() {
        // Arrange
        String nonExistentReviewId = UUID.randomUUID().toString();
        when(reviewRepository.findById(nonExistentReviewId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.approveReview(nonExistentReviewId);
        });
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testFindByIsVerifiedTrue_ReturnsVerifiedReviews() {
        // Arrange
        testReview.setVerified(true);
        when(reviewRepository.findByIsVerifiedTrue()).thenReturn(List.of(testReview));

        // Act
        List<Review> result = reviewService.findByIsVerifiedTrue();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).isVerified());
        verify(reviewRepository, times(1)).findByIsVerifiedTrue();
    }
}