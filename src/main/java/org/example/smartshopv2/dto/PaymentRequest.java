package org.example.smartshopv2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Payment amount is required")
    @Min(value = 1, message = "Amount must be positive")
    private Double montant;
    
    @NotBlank(message = "Payment type is required (ESPECES, CHEQUE, VIREMENT)")
    private String typePaiement;
    
    private String reference;
    
    private String banque;
    
    private LocalDateTime dateEcheance;
    
    private LocalDateTime datePaiement;
}
