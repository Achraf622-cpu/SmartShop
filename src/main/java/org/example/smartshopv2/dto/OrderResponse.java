package org.example.smartshopv2.dto;

import lombok.Data;
import org.example.smartshopv2.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long clientId;
    private String clientName;
    private List<OrderItemResponse> items;
    private Double subtotalHT;
    private String promoCode;
    private Double discountAmount;
    private Double amountAfterDiscount;
    private Double tva;
    private Double totalTTC;
    private Double montantRestant;
    private List<PaymentResponse> payments;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
