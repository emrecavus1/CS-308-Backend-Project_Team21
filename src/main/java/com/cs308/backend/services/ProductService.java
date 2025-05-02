package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, UserRepository userRepository, ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    public ResponseEntity<String> addProduct(Product product, String name, String info, String categoryName, int stock, String serialNumber, String warrantyStatus, String distributorInfo) {
        if (productRepository.findByProductNameIgnoreCase(name) != null) {
            return ResponseEntity.badRequest().body("Product with this name already exists!");
        }

        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body("Product name cannot be empty!");
        }
        product.setProductName(name);
        if (info == null || info.isEmpty()) {
            return ResponseEntity.badRequest().body("Product info cannot be empty!");
        }
        product.setProductInfo(info);

        if (categoryName == null || categoryName.isEmpty()) {
            return ResponseEntity.badRequest().body("Product category name cannot be empty!");
        }
        Optional<Category> categoryOptional = categoryRepository.findByCategoryNameIgnoreCase(categoryName);

        if (!categoryOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Category does not exist!");
        }
        Category category = categoryOptional.get();
        product.setCategoryId(category.getCategoryId());

        if (stock < 0) {
            return ResponseEntity.badRequest().body("Stock cannot be negative!");
        }
        product.setStockCount(stock);

        if (serialNumber == null || serialNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Serial number cannot be empty!");
        }
        product.setSerialNumber(serialNumber);

        if (warrantyStatus == null || warrantyStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Warranty status cannot be empty!");
        }
        product.setWarrantyStatus(warrantyStatus);

        if (distributorInfo == null || distributorInfo.isEmpty()) {
            return ResponseEntity.badRequest().body("Distributor info cannot be empty!");
        }
        // 1) generate your own ID:
        product.setProductId(UUID.randomUUID().toString());


        product.setDistributorInfo(distributorInfo);

        Product savedProduct = productRepository.save(product);
        category.getProductIds().add(savedProduct.getProductId()); // Add the newly created product's ID
        categoryRepository.save(category);  // Save the updated category

        return ResponseEntity.ok("Product added successfully!");
    }

    public Optional<Product> getProductById(String productId) {
        return productRepository.findById(productId);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Product searchProductByName(String productName) {
        return productRepository.findByProductNameIgnoreCase(productName);
    }

    public Product updateStock(String productId, int newStock) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setStockCount(newStock);
            return productRepository.save(product);
        } else {
            throw new NoSuchElementException("Product not found!");
        }
    }


    public Product setPrice(String productId, double price) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setPrice(price);
            return productRepository.save(product);
        } else {
            Product newProduct = new Product();
            newProduct.setProductId(productId);
            newProduct.setPrice(price);
            return productRepository.save(newProduct); // You may want to populate required fields too
        }
    }


    public Product setName(String productId, String productName) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setProductName(productName);
            return productRepository.save(product);
        }
        else {
            throw new NoSuchElementException("Product not found!");
        }
    }


    public Product updateProduct(String productId, Map<String, Object> updates) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();

            // Update product name if provided
            if (updates.containsKey("productName")) {
                product.setProductName((String) updates.get("productName"));
            }

            // Update product info if provided
            if (updates.containsKey("productInfo")) {
                product.setProductInfo((String) updates.get("productInfo"));
            }

            // Update price if provided
            if (updates.containsKey("price")) {
                // Convert the value to a double. Adjust as needed if your client sends a different type.
                product.setPrice(Double.parseDouble(updates.get("price").toString()));
            }

            // Update stock count if provided
            if (updates.containsKey("stockCount")) {
                product.setStockCount(Integer.parseInt(updates.get("stockCount").toString()));
            }


            if (updates.containsKey("serialNumber")) {
                product.setSerialNumber((String) updates.get("serialNumber"));
            }

            if (updates.containsKey("warrantyStatus")) {
                product.setWarrantyStatus((String) updates.get("warrantyStatus"));
            }

            if (updates.containsKey("distributorInfo")) {
                product.setDistributorInfo((String) updates.get("distributorInfo"));
            }

            return productRepository.save(product);
        } else {
            throw new NoSuchElementException("Product not found with ID: " + productId);
        }
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByProductNameContainingIgnoreCaseOrProductInfoContainingIgnoreCase(query, query)
                .stream()
                .filter(p -> p.getPrice() > 0)
                .toList();
    }


    public List<Product> sortProductsByPrice() {
        List<Product> products = getAllProducts();
        products.sort(Comparator.comparing(Product::getPrice));
        return products;
    }

    public List<Product> sortProductsByRating() {
        List<Product> products = getAllProducts();
        products.sort(Comparator.comparing(Product::getRating).reversed());
        return products;
    }

    public void deleteProductWithCleanup(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));

        // 1. Remove the product ID from its category
        Category category = categoryRepository.findById(product.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Category not found for product"));
        if (category.getProductIds() != null) {
            category.getProductIds().remove(productId);
            categoryRepository.save(category);
        }

        // 2. Delete all associated reviews
        List<Review> reviews = reviewRepository.findByProductId(productId);
        for (Review r : reviews) {
            reviewRepository.deleteById(r.getReviewId());
        }

        // 3. Remove from all user wishlists
        List<User> users = userRepository.findAll();
        for (User u : users) {
            if (u.getWishList() != null && u.getWishList().contains(productId)) {
                u.getWishList().remove(productId);
                userRepository.save(u);
            }
        }

        // 4. Delete the product itself
        productRepository.deleteById(productId);
    }


}