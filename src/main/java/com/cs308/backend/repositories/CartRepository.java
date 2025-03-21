package com.cs308.backend.repositories;

import com.cs308.backend.models.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String>
{
    // Find a cart by userId
    Optional<Cart> findByUserId(String userId);

    // Delete a cart by userId
    void deleteByUserId(String userId);
}