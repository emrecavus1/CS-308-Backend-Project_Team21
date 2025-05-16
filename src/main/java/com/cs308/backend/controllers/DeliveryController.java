package com.cs308.backend.controllers;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;
import com.cs308.backend.services.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cs308.backend.exception.UserAlreadyExistsException;


@RestController
@RequestMapping("/api/manager")
public class DeliveryController {
    private final ProductRepository prodRepo;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;

    @Autowired
    public DeliveryController(ProductRepository prodRepo, UserRepository userRepo, OrderRepository orderRepo) {
        this.prodRepo = prodRepo;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
    }

    @GetMapping("/delivery-details")
    public ResponseEntity<List<Map<String, Object>>> getDeliveryDetailsByOrderId(@RequestParam String orderId) {
        Optional<Order> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Order order = orderOpt.get();

        Optional<User> userOpt = userRepo.findById(order.getUserId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        List<Product> products = prodRepo.findAllById(order.getProductIds());
        List<Integer> quantities = order.getQuantities();

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            int qty = quantities.get(i);

            Map<String, Object> entry = new HashMap<>();
            entry.put("productId", p.getProductId());
            entry.put("productName", p.getProductName());
            entry.put("quantity", qty);
            entry.put("totalPrice", qty * p.getPrice());
            entry.put("deliveryAddress", user.getSpecificAddress() + ", " + user.getCity());
            entry.put("delivered", order.isShipped());

            result.add(entry);
        }

        return ResponseEntity.ok(result);
    }

}