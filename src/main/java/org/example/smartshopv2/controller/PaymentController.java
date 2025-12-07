package org.example.smartshopv2.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.dto.PaymentRequest;
import org.example.smartshopv2.dto.PaymentResponse;
import org.example.smartshopv2.service.AuthorizationService;
import org.example.smartshopv2.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    private final AuthorizationService authService;
    
    @PostMapping
    public ResponseEntity<?> addPayment(@Valid @RequestBody PaymentRequest request, HttpSession session) {
        try {
            // Only ADMIN can add payments
            authService.requireAdmin(session);
            PaymentResponse response = paymentService.addPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/encaisser")
    public ResponseEntity<?> encaisserPayment(@PathVariable Long id, HttpSession session) {
        try {
            // Only ADMIN can encaisser payments
            authService.requireAdmin(session);
            PaymentResponse response = paymentService.encaisserPayment(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterPayment(@PathVariable Long id, HttpSession session) {
        try {
            // Only ADMIN can reject payments
            authService.requireAdmin(session);
            PaymentResponse response = paymentService.rejeterPayment(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderPayments(@PathVariable Long orderId, HttpSession session) {
        try {
            // Only ADMIN can view order payments
            authService.requireAdmin(session);
            List<PaymentResponse> payments = paymentService.getOrderPayments(orderId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getPayment(@PathVariable Long id, HttpSession session) {
        try {
            // Only ADMIN can view payment details
            authService.requireAdmin(session);
            PaymentResponse response = paymentService.getPayment(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
