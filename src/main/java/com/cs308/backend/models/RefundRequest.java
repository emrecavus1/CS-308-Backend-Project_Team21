package com.cs308.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "refund_requests")
public class RefundRequest {

    @Id
    private String requestId;

    private String orderId;
    private String userId;

    private LocalDateTime requestDate;

    private boolean processed;

    private List<RefundItem> items;
}
