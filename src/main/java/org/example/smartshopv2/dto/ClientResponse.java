package org.example.smartshopv2.dto;

import lombok.Data;
import org.example.smartshopv2.enums.LoyaltyLevel;

import java.time.LocalDateTime;

@Data
public class ClientResponse {
    private Long id;
    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private String address;
    private LoyaltyLevel loyaltyLevel;
    private Integer totalOrders;
    private Double totalSpent;
    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;
    private LocalDateTime createdAt;
}
