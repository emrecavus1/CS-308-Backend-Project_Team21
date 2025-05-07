package com.cs308.backend.repositories;

import com.cs308.backend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email); // ✅ Added this method

    // Add this new method to find users who have a specific product in their wishlist
    List<User> findByWishListContains(String productId);

}
