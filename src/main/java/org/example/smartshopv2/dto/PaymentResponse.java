package org.example.smartshopv2.dto;

import lombok.Data;
import org.example.smartshopv2.enums.PaymentStatus;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Integer numeroPaiement;
    private Double montant;
    private String typePaiement;
    private String reference;
    private String banque;
    private LocalDateTime datePaiement;
    private LocalDateTime dateEcheance;
    private LocalDateTime dateEncaissement;
    private PaymentStatus status;
}
