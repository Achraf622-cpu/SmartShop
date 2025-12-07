package org.example.smartshopv2.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.dto.OrderRequest;
import org.example.smartshopv2.dto.OrderResponse;
import org.example.smartshopv2.service.AuthorizationService;
import org.example.smartshopv2.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final AuthorizationService authService;
    
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request, HttpSession session) {
        try {
            // Only ADMIN can create orders (per requirements)
            authService.requireAdmin(session);
            
            // Extract clientId from request
            Long clientId = request.getClientId();
            OrderResponse response = orderService.createOrder(clientId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id, HttpSession session) {
        try {
            // Anyone authenticated can view orders
            authService.requireAuthenticated(session);
            OrderResponse response = orderService.getOrder(id);
            
            // If CLIENT, verify they own this order
            if (!authService.isAdmin(session)) {
                Long clientId = authService.getClientId(session);
                if (clientId == null || !response.getClientId().equals(clientId)) {
                    throw new RuntimeException("Access denied. You can only view your own orders.");
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllOrders(HttpSession session) {
        try {
            // Only ADMIN can view all orders
            authService.requireAdmin(session);
            List<OrderResponse> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id, HttpSession session) {
        try {
            // Only ADMIN can confirm orders
            authService.requireAdmin(session);
            OrderResponse response = orderService.confirmOrder(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, HttpSession session) {
        try {
            // Only ADMIN can cancel orders
            authService.requireAdmin(session);
            OrderResponse response = orderService.cancelOrder(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
