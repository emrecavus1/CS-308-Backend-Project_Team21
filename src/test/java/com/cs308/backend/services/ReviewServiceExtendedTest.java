
package com.cs308.backend.services;

import com.cs308.backend.models.Product;
import com.cs308.backend.models.Review;
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

public class ReviewServiceExtendedTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

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
        testProduct.setRating(0.0);

        testReview = new Review();
        testReview.setReviewId(UUID.randomUUID().toString());
        testReview.setUserId(UUID.randomUUID().toString());
        testReview.setProductId(testProduct.getProductId());
        testReview.setRating(3.0);
        testReview.setComment("Decent product.");
        testReview.setVerified(false);
    }


    @Test
    public void testDeclineReview_Successful() {
        testReview.setVerified(true);
        when(reviewRepository.findById(testReview.getReviewId())).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        Review result = reviewService.declineReview(testReview.getReviewId());

        assertFalse(result.isVerified());
        verify(reviewRepository, times(1)).save(testReview);
    }

    @Test
    public void testDeclineReview_NotFound() {
        String invalidId = UUID.randomUUID().toString();
        when(reviewRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.declineReview(invalidId);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testPostReview_RecalculatesAverageRatingCorrectly() {
        Review secondReview = new Review();
        secondReview.setReviewId(UUID.randomUUID().toString());
        secondReview.setUserId(UUID.randomUUID().toString());
        secondReview.setProductId(testProduct.getProductId());
        secondReview.setRating(5.0);
        secondReview.setVerified(true);

        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(productRepository.findById(testReview.getProductId())).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(reviewRepository.findByProductId(testProduct.getProductId()))
                .thenReturn(List.of(testReview, secondReview));

        Review result = reviewService.postReview(testReview);

        assertEquals(4.0, testProduct.getRating());
        assertEquals(testReview.getReviewId(), result.getReviewId());
        verify(productRepository, atLeastOnce()).save(testProduct);
    }
}
