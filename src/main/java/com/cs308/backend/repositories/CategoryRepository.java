package com.cs308.backend.repositories;

import com.cs308.backend.models.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    // Find category by name (to prevent duplicates)
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);

    // Check if a category with this name exists
    boolean existsByCategoryName(String categoryName);
}
