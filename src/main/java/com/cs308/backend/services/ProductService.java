package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

import com.cs308.backend.mailing.DefaultEmailService;
import com.cs308.backend.mailing.DiscountEmailContext;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    private DefaultEmailService emailService;

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

        // Set default production cost if not manually specified
        if (product.getProductionCost() == null) {
            product.setProductionCost(product.getPrice() * 0.5);
        }

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

            // Update production cost if it was using the default value (50% of price)
            // Check if production cost is null or if it was exactly 50% of the old price
            if (product.getProductionCost() == null ||
                    Math.abs(product.getProductionCost() - (product.getPrice() * 0.5)) < 0.01) {
                product.setProductionCost(price * 0.5);
            }

            return productRepository.save(product);
        } else {
            Product newProduct = new Product();
            newProduct.setProductId(productId);
            newProduct.setPrice(price);
            // Set default production cost for new product
            newProduct.setProductionCost(price * 0.5);
            return productRepository.save(newProduct);
        }
    }

    // New method to set production cost manually
    public Product setProductionCost(String productId, double productionCost) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();

            // Validate production cost (optional: add your business rules here)
            if (productionCost < 0) {
                throw new IllegalArgumentException("Production cost cannot be negative");
            }

            product.setProductionCost(productionCost);
            return productRepository.save(product);
        } else {
            throw new NoSuchElementException("Product not found with ID: " + productId);
        }
    }

    // New method to reset production cost to default (50% of price)
    public Product resetProductionCostToDefault(String productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setProductionCost(product.getPrice() * 0.5);
            return productRepository.save(product);
        } else {
            throw new NoSuchElementException("Product not found with ID: " + productId);
        }
    }

    // New method to get production cost
    public double getProductionCost(String productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            if (product.getProductionCost() == null) {
                return product.getPrice() * 0.5; // Return default if not set
            }
            return product.getProductionCost();
        } else {
            throw new NoSuchElementException("Product not found with ID: " + productId);
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
                double oldPrice = product.getPrice();
                double newPrice = Double.parseDouble(updates.get("price").toString());
                product.setPrice(newPrice);

                // Update production cost if it was using the default value (50% of price)
                // Check if production cost is null or if it was exactly 50% of the old price
                if (product.getProductionCost() == null ||
                        Math.abs(product.getProductionCost() - (oldPrice * 0.5)) < 0.01) {
                    product.setProductionCost(newPrice * 0.5);
                }
            }

            // Update production cost if provided
            if (updates.containsKey("productionCost")) {
                double productionCost = Double.parseDouble(updates.get("productionCost").toString());
                if (productionCost < 0) {
                    throw new IllegalArgumentException("Production cost cannot be negative");
                }
                product.setProductionCost(productionCost);
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

    // New method to sort products by profit margin (price - production cost)
    public List<Product> sortProductsByProfitMargin() {
        List<Product> products = getAllProducts();
        products.sort(Comparator.comparing((Product p) -> {
            double productionCost = p.getProductionCost() != null ? p.getProductionCost() : p.getPrice() * 0.5;
            return p.getPrice() - productionCost;
        }).reversed());
        return products;
    }

    // New method to sort products by profit margin percentage
    public List<Product> sortProductsByProfitMarginPercentage() {
        List<Product> products = getAllProducts();
        products.sort(Comparator.comparing((Product p) -> {
            double productionCost = p.getProductionCost() != null ? p.getProductionCost() : p.getPrice() * 0.5;
            if (productionCost == 0) return Double.MAX_VALUE; // Avoid division by zero
            return (p.getPrice() - productionCost) / productionCost;
        }).reversed());
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

    public Product setDiscount(String productId, double discountPercentage) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();

            if (discountPercentage <= 0 || discountPercentage > 99) {
                throw new IllegalArgumentException("Discount percentage must be between 1 and 99");
            }

            double discountedPrice = product.getPrice() * (1 - discountPercentage / 100);
            product.setPrice(discountedPrice);

            return productRepository.save(product);
        } else {
            throw new NoSuchElementException("Product not found with ID: " + productId);
        }
    }

    public Map<String, Object> setDiscountAndNotifyUsers(String productId, double discountPercentage) {
        // Step 1: Fetch product before modifying
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + productId));

        double originalPrice = product.getPrice();
        double discountedPrice = originalPrice * (1 - discountPercentage / 100);

        // Step 2: Update and save discounted product
        product.setPrice(discountedPrice);
        Product updated = productRepository.save(product);

        // Step 3: Pass the original price manually
        int notifiedUsers = notifyUsersAboutDiscount(product, discountPercentage, originalPrice);

        return Map.of("product", updated, "notifiedUsers", notifiedUsers);
    }


    public int notifyUsersAboutDiscount(Product product, double discountPercentage, double originalPrice) {
        List<User> users = userRepository.findByWishListContains(product.getProductId());

        double discountedPrice = product.getPrice();
        int notified = 0;

        for (User user : users) {
            try {
                DiscountEmailContext emailContext = new DiscountEmailContext();
                emailContext.initWithDetails(
                        user.getName(),
                        product.getProductName(),
                        discountPercentage,
                        originalPrice,
                        discountedPrice,
                        product.getProductId()
                );
                emailContext.setFrom("noreply@yourstore.com");
                emailContext.setTo(user.getEmail());

                emailService.sendMail(emailContext);
                notified++;
            } catch (Exception e) {
                System.err.println("❌ Failed to notify " + user.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return notified;
    }


    public Product removeDiscount(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + productId));

        throw new UnsupportedOperationException("Original price tracking has been removed. Cannot 'remove' discount without restoring original.");
    }


    public List<Product> getNewProducts() {
        return productRepository.findAll()
                .stream()
                .filter(p -> p.getPrice() == 0.0)
                .toList();
    }

    public int patchMissingProductionCosts() {
        List<Product> all = productRepository.findAll();
        int patched = 0;

        for (Product p : all) {
            if (p.getProductionCost() == null) {
                p.setProductionCost(p.getPrice() * 0.5);
                productRepository.save(p);
                patched++;
            }
        }

        return patched;
    }


    public Product setPriceExplicit(String productId, double price) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));

        product.setPrice(price);
        product.setProductionCost(price * 0.5); // Always reset to 50%
        return productRepository.save(product);
    }




}