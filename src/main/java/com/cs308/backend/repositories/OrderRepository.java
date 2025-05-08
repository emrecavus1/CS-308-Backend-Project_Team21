package com.cs308.backend.repositories;

import com.cs308.backend.models.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String>
{
    // Find all orders placed by a specific user
    List<Order> findByUserId(String userId);

    // Find order by cart ID
    Optional<Order> findByCartId(String cartId);

    // Find order by paymentId
    Optional<Order> findByPaymentId(String paymentId);

    // Get orders by paid status
    List<Order> findByPaid(boolean paid);

    // Get orders by shipped status
    List<Order> findByShipped(boolean shipped);

    // Get orders by status string (optional if you're using it like "pending", "completed", etc.)
    List<Order> findByStatus(String status);

    List<Order> findByUserIdAndShippedTrue(String userId);

    List<Order> findByUserIdAndShippedFalse(String userId);

    List<Order> findByPaidIsTrueAndInvoicePathIsNotNull();

    // Case-insensitive version of status filter
    List<Order> findByStatusIgnoreCase(String status);

}