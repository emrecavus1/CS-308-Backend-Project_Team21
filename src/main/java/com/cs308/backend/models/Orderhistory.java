package com.cs308.backend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "order_history")
public class OrderHistory {
    @Id
    private String orderHistoryId; // Unique ID for this order history record
    private String userId;         // Reference to the user that placed the orders
    private List<String> orderIds; // List of order IDs that belong to this user
}
