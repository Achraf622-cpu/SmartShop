package org.example.smartshopv2.dto;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double priceHT;
    private Double subtotal;
}
