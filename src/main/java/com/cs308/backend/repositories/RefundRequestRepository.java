package com.cs308.backend.repositories;

import com.cs308.backend.models.RefundRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRequestRepository extends MongoRepository<RefundRequest, String> {
    List<RefundRequest> findByProcessed(boolean processed);

    // To support refund filtering by orderId
    List<RefundRequest> findByOrderId(String orderId);

    // To support filtering refund requests by user
    List<RefundRequest> findByUserId(String userId);

}
