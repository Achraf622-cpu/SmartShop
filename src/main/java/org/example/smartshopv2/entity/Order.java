package org.example.smartshopv2.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.smartshopv2.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @Column(nullable = false)
    private Double subtotalHT;
    
    private String promoCode;
    
    @Column(nullable = false)
    private Double discountAmount = 0.0;
    
    @Column(nullable = false)
    private Double amountAfterDiscount;
    
    @Column(nullable = false)
    private Double tva;
    
    @Column(nullable = false)
    private Double totalTTC;
    
    @Column(nullable = false)
    private Double montantRestant;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Payment> payments = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
}
