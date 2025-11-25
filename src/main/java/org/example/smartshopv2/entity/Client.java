package org.example.smartshopv2.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.smartshopv2.enums.LoyaltyLevel;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Data
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String companyName;
    
    private String contactName;
    
    private String email;
    
    private String phone;
    
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoyaltyLevel loyaltyLevel = LoyaltyLevel.BASIC;
    
    @Column(nullable = false)
    private Integer totalOrders = 0;
    
    @Column(nullable = false)
    private Double totalSpent = 0.0;
    
    private LocalDateTime firstOrderDate;
    
    private LocalDateTime lastOrderDate;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
