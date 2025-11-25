package org.example.smartshopv2.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.smartshopv2.enums.PaymentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(nullable = false)
    private Integer numeroPaiement;
    
    @Column(nullable = false)
    private Double montant;
    
    @Column(nullable = false)
    private String typePaiement;
    
    private String reference;
    
    private String banque;
    
    @Column(nullable = false)
    private LocalDateTime datePaiement;
    
    private LocalDateTime dateEcheance;
    
    private LocalDateTime dateEncaissement;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.EN_ATTENTE;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
