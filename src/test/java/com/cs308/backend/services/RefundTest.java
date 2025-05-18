package com.cs308.backend.services;  // or wherever you keep it

import com.cs308.backend.controllers.OrderController;
import com.cs308.backend.models.RefundItem;
import com.cs308.backend.models.RefundRequest;
import com.cs308.backend.services.CartService;
import com.cs308.backend.services.OrderHistoryService;
import com.cs308.backend.services.OrderService;
import com.cs308.backend.services.PaymentService;
import com.cs308.backend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RefundTest {

    private MockMvc mockMvc;

    @Mock private UserService userService;
    @Mock private CartService cartService;
    @Mock private PaymentService paymentService;
    @Mock private OrderService orderService;
    @Mock private OrderHistoryService orderHistoryService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setup() {
        // standaloneSetup does NOT pull in your Spring Security filters,
        // so PreAuthorize won’t block you here.
        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .build();
    }

    @Test
    void requestRefund_ShouldReturnOkAndMessage() throws Exception {
        String orderId   = "order123";
        String userId    = "user456";
        String productId = "prod789";
        String quantity  = "2";
        String expected  = "Refund requested";

        when(orderService.requestRefundSingle(orderId, userId, productId, quantity))
                .thenReturn(ResponseEntity.ok(expected));

        mockMvc.perform(put("/api/order/requestRefund/{orderId}", orderId)
                        .param("userId",    userId)
                        .param("productId", productId)
                        .param("quantity",  quantity))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }

    @Test
    void getActiveRefundRequests_ShouldReturnListAsJson() throws Exception {
        RefundItem item = new RefundItem("prod1", 1, 10.0);
        RefundRequest req = new RefundRequest(
                "req123",
                "order123",
                "user456",
                LocalDateTime.of(2025, 5, 1, 12, 0),
                false,
                Collections.singletonList(item)
        );

        when(orderService.getRefundRequestsByProcessed(false))
                .thenReturn(Collections.singletonList(req));

        mockMvc.perform(get("/api/order/refundRequests/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestId").value("req123"))
                .andExpect(jsonPath("$[0].orderId").value("order123"))
                .andExpect(jsonPath("$[0].items[0].productId").value("prod1"))
                .andExpect(jsonPath("$[0].items[0].quantity").value(1));
    }

    @Test
    void approveRefund_ShouldReturnOkAndApprovalMessage() throws Exception {
        String requestId = "req123";
        String approved  = "Approved";

        when(orderService.approveRefund(requestId))
                .thenReturn(ResponseEntity.ok(approved));

        mockMvc.perform(put("/api/order/refund/approve/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(content().string(approved));
    }

    @Test
    void rejectRefund_ShouldReturnOkAndRejectionMessage() throws Exception {
        String requestId = "req123";
        String rejected  = "Rejected";

        when(orderService.rejectRefund(requestId))
                .thenReturn(ResponseEntity.ok(rejected));

        mockMvc.perform(delete("/api/order/refund/reject/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(content().string(rejected));
    }

    // 1) requestRefund with missing required parameter → 400 Bad Request
    @Test
    void requestRefund_MissingParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/order/requestRefund/{orderId}", "order123")
                        // omit userId/productId/quantity
                )
                .andExpect(status().isBadRequest());
    }

    // 2) requestRefund when service returns 400 → Bad Request
    @Test
    void requestRefund_ServiceReturnsBadRequest_ShouldReturnBadRequest() throws Exception {
        when(orderService.requestRefundSingle(any(), any(), any(), any()))
                .thenReturn(ResponseEntity.badRequest().body("Invalid refund"));
        mockMvc.perform(put("/api/order/requestRefund/{orderId}", "order123")
                        .param("userId", "u1")
                        .param("productId", "p1")
                        .param("quantity", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid refund"));
    }

    // 3) requestRefund when service throws → 500 Internal Server Error
    @Test
    void requestRefund_ServiceThrowsException_ShouldReturnServerError() throws Exception {
        when(orderService.requestRefundSingle(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));
        mockMvc.perform(put("/api/order/requestRefund/{orderId}", "order123")
                        .param("userId", "u1")
                        .param("productId", "p1")
                        .param("quantity", "1"))
                .andExpect(status().isInternalServerError());
    }

    // 4) getActiveRefundRequests with no data → empty JSON array
    @Test
    void getActiveRefundRequests_NoRequests_ShouldReturnEmptyList() throws Exception {
        when(orderService.getRefundRequestsByProcessed(false))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/order/refundRequests/active"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // 5) approveRefund for non-existent ID → 404 Not Found
    @Test
    void approveRefund_NonexistentId_ShouldReturnNotFound() throws Exception {
        when(orderService.approveRefund("nope"))
                .thenReturn(ResponseEntity.notFound().build());
        mockMvc.perform(put("/api/order/refund/approve/{requestId}", "nope"))
                .andExpect(status().isNotFound());
    }

    // 6) rejectRefund for non-existent ID → 404 Not Found
    @Test
    void rejectRefund_NonexistentId_ShouldReturnNotFound() throws Exception {
        when(orderService.rejectRefund("nope"))
                .thenReturn(ResponseEntity.notFound().build());
        mockMvc.perform(delete("/api/order/refund/reject/{requestId}", "nope"))
                .andExpect(status().isNotFound());
    }

}
