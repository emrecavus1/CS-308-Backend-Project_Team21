package com.cs308.backend.repositories;

import com.cs308.backend.models.OrderHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderHistoryRepository extends MongoRepository<OrderHistory, String> {

    // Finds the order history for a given user by their userId
    Optional<OrderHistory> findByUserId(String userId);
}
