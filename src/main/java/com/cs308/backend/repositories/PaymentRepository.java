package com.cs308.backend.repositories;

import com.cs308.backend.models.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    // Find a payment by associated order ID
    Optional<Payment> findByOrderId(String orderId);

    // Find all payments made by a specific user
    List<Payment> findByUserId(String userId);

    // Optional: Find payment by card number (not recommended for production use due to sensitivity)
    Optional<Payment> findByCardNumber(String cardNumber);
}
