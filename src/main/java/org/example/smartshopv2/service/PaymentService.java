package org.example.smartshopv2.service;

import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.dto.PaymentRequest;
import org.example.smartshopv2.dto.PaymentResponse;
import org.example.smartshopv2.entity.Order;
import org.example.smartshopv2.entity.Payment;
import org.example.smartshopv2.enums.PaymentStatus;
import org.example.smartshopv2.repository.OrderRepository;
import org.example.smartshopv2.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    
    private static final double ESPECES_MAX = 20000.0;
    
    @Transactional
    public PaymentResponse addPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Validate order is still PENDING
        if (order.getStatus() != org.example.smartshopv2.enums.OrderStatus.PENDING) {
            throw new RuntimeException("Can only add payments to PENDING orders");
        }
        
        // Validate payment amount
        if (request.getMontant() > order.getMontantRestant()) {
            throw new RuntimeException("Payment amount exceeds remaining balance");
        }
        
        // Validate ESPECES limit (20,000 DH max)
        if ("ESPECES".equalsIgnoreCase(request.getTypePaiement()) && request.getMontant() > ESPECES_MAX) {
            throw new RuntimeException("Cash payment cannot exceed 20,000 DH (Art. 193 CGI)");
        }
        
        // Create payment
        Payment payment = new Payment();
        payment.setOrder(order);
        
        // Calculate payment number (sequential)
        int nextNumber = order.getPayments().size() + 1;
        payment.setNumeroPaiement(nextNumber);
        
        payment.setMontant(request.getMontant());
        payment.setTypePaiement(request.getTypePaiement().toUpperCase());
        payment.setReference(request.getReference());
        payment.setBanque(request.getBanque());
        payment.setDateEcheance(request.getDateEcheance());
        
        // Set payment date (default to now if not provided)
        payment.setDatePaiement(request.getDatePaiement() != null ? 
                request.getDatePaiement() : LocalDateTime.now());
        
        // Set status based on payment type
        if ("ESPECES".equalsIgnoreCase(request.getTypePaiement())) {
            payment.setStatus(PaymentStatus.ENCAISSE);
            payment.setDateEncaissement(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.EN_ATTENTE);
        }
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Update order remaining amount
        order.setMontantRestant(order.getMontantRestant() - request.getMontant());
        orderRepository.save(order);
        
        return mapToResponse(savedPayment);
    }
    
    @Transactional
    public PaymentResponse encaisserPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (payment.getStatus() == PaymentStatus.ENCAISSE) {
            throw new RuntimeException("Payment already encaisse");
        }
        
        if (payment.getStatus() == PaymentStatus.REJETE) {
            throw new RuntimeException("Cannot encaisser a rejected payment");
        }
        
        payment.setStatus(PaymentStatus.ENCAISSE);
        payment.setDateEncaissement(LocalDateTime.now());
        
        Payment updated = paymentRepository.save(payment);
        return mapToResponse(updated);
    }
    
    @Transactional
    public PaymentResponse rejeterPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (payment.getStatus() == PaymentStatus.ENCAISSE) {
            throw new RuntimeException("Cannot reject an encaisse payment");
        }
        
        Order order = payment.getOrder();
        
        // Restore remaining amount
        order.setMontantRestant(order.getMontantRestant() + payment.getMontant());
        orderRepository.save(order);
        
        payment.setStatus(PaymentStatus.REJETE);
        
        Payment updated = paymentRepository.save(payment);
        return mapToResponse(updated);
    }
    
    public List<PaymentResponse> getOrderPayments(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderIdOrderByNumeroPaiementAsc(orderId);
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return mapToResponse(payment);
    }
    
    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder().getId());
        response.setNumeroPaiement(payment.getNumeroPaiement());
        response.setMontant(payment.getMontant());
        response.setTypePaiement(payment.getTypePaiement());
        response.setReference(payment.getReference());
        response.setBanque(payment.getBanque());
        response.setDatePaiement(payment.getDatePaiement());
        response.setDateEcheance(payment.getDateEcheance());
        response.setDateEncaissement(payment.getDateEncaissement());
        response.setStatus(payment.getStatus());
        return response;
    }
}
