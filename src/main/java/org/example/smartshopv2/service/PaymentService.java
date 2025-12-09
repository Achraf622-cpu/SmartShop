package org.example.smartshopv2.service;

import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.dto.PaymentRequest;
import org.example.smartshopv2.dto.PaymentResponse;
import org.example.smartshopv2.entity.Order;
import org.example.smartshopv2.entity.Payment;
import org.example.smartshopv2.enums.PaymentStatus;
import org.example.smartshopv2.mapper.PaymentMapper;
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
    private final PaymentMapper paymentMapper;

    private static final double ESPECES_MAX = 20000.0;

    @Transactional
    public PaymentResponse addPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validate order is still PENDING
        if (order.getStatus() != org.example.smartshopv2.enums.OrderStatus.PENDING) {
            throw new RuntimeException("Can only add payments to PENDING orders");
        }

        // Validate payment type
        String typePaiement = request.getTypePaiement().toUpperCase();
        if (!isValidPaymentType(typePaiement)) {
            throw new RuntimeException("Invalid payment type. Allowed types: ESPECES, CHEQUE, VIREMENT, CARTE");
        }

        // Validate payment amount
        if (request.getMontant() > order.getMontantRestant()) {
            throw new RuntimeException("Payment amount exceeds remaining balance");
        }

        // Validate ESPECES limit (20,000 DH max)
        if ("ESPECES".equals(typePaiement) && request.getMontant() > ESPECES_MAX) {
            throw new RuntimeException("Cash payment cannot exceed 20,000 DH (Art. 193 CGI)");
        }

        // Calculate payment number (sequential)
        int nextNumber = order.getPayments().size() + 1;

        // Determine status and encaissement date based on payment type
        PaymentStatus status;
        LocalDateTime dateEncaissement = null;
        if ("ESPECES".equalsIgnoreCase(request.getTypePaiement())) {
            status = PaymentStatus.ENCAISSE;
            dateEncaissement = LocalDateTime.now();
        } else {
            status = PaymentStatus.EN_ATTENTE;
        }

        // Create payment using builder
        Payment payment = Payment.builder()
                .order(order)
                .numeroPaiement(nextNumber)
                .montant(request.getMontant())
                .typePaiement(request.getTypePaiement().toUpperCase())
                .reference(request.getReference())
                .banque(request.getBanque())
                .dateEcheance(request.getDateEcheance())
                .datePaiement(request.getDatePaiement() != null ? request.getDatePaiement() : LocalDateTime.now())
                .status(status)
                .dateEncaissement(dateEncaissement)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Update order remaining amount
        order.setMontantRestant(order.getMontantRestant() - request.getMontant());
        orderRepository.save(order);

        return paymentMapper.toResponse(savedPayment);
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
        return paymentMapper.toResponse(updated);
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
        return paymentMapper.toResponse(updated);
    }

    public List<PaymentResponse> getOrderPayments(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderIdOrderByNumeroPaiementAsc(orderId);
        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return paymentMapper.toResponse(payment);
    }

    private static final java.util.Set<String> VALID_PAYMENT_TYPES = java.util.Set.of(
            "ESPECES", "CHEQUE", "VIREMENT", "CARTE");

    private boolean isValidPaymentType(String type) {
        return VALID_PAYMENT_TYPES.contains(type);
    }
}
