package org.example.smartshopv2.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.dto.ClientResponse;
import org.example.smartshopv2.dto.OrderResponse;
import org.example.smartshopv2.service.AuthorizationService;
import org.example.smartshopv2.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints for logged-in clients to access their own data.
 * Uses session to automatically identify the current client.
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ClientService clientService;
    private final AuthorizationService authService;

    /**
     * Get current client's profile (stats included)
     * GET /api/me/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(HttpSession session) {
        try {
            authService.requireClient(session);
            Long clientId = authService.getClientId(session);

            if (clientId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Client profile not found for this user"));
            }

            ClientResponse profile = clientService.getClient(clientId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get current client's order history
     * GET /api/me/orders
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(HttpSession session) {
        try {
            authService.requireClient(session);
            Long clientId = authService.getClientId(session);

            if (clientId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Client profile not found for this user"));
            }

            List<OrderResponse> orders = clientService.getClientOrders(clientId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get current client's statistics summary
     * GET /api/me/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getMyStats(HttpSession session) {
        try {
            authService.requireClient(session);
            Long clientId = authService.getClientId(session);

            if (clientId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Client profile not found for this user"));
            }

            ClientResponse client = clientService.getClient(clientId);

            // Return a focused stats view
            return ResponseEntity.ok(Map.of(
                    "companyName", client.getCompanyName(),
                    "loyaltyLevel", client.getLoyaltyLevel(),
                    "totalOrders", client.getTotalOrders(),
                    "totalSpent", client.getTotalSpent(),
                    "firstOrderDate", client.getFirstOrderDate() != null ? client.getFirstOrderDate() : "No orders yet",
                    "lastOrderDate", client.getLastOrderDate() != null ? client.getLastOrderDate() : "No orders yet",
                    "memberSince", client.getCreatedAt()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
