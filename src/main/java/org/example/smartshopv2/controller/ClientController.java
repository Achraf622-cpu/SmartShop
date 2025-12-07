package org.example.smartshopv2.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.dto.ClientRequest;
import org.example.smartshopv2.dto.ClientResponse;
import org.example.smartshopv2.dto.OrderResponse;
import org.example.smartshopv2.service.AuthorizationService;
import org.example.smartshopv2.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private final ClientService clientService;
    private final AuthorizationService authService;
    
    @PostMapping
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientRequest request, HttpSession session) {
        try {
            // Only ADMIN can create clients
            authService.requireAdmin(session);
            ClientResponse response = clientService.createClient(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getClient(@PathVariable Long id, HttpSession session) {
        try {
            // Only owner or ADMIN can view client details
            authService.requireOwnerOrAdmin(session, id);
            ClientResponse response = clientService.getClient(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllClients(HttpSession session) {
        try {
            // Only ADMIN can view all clients
            authService.requireAdmin(session);
            List<ClientResponse> clients = clientService.getAllClients();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, 
                                          @Valid @RequestBody ClientRequest request,
                                          HttpSession session) {
        try {
            // Only owner or ADMIN can update client
            authService.requireOwnerOrAdmin(session, id);
            ClientResponse response = clientService.updateClient(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Long id, HttpSession session) {
        try {
            // Only ADMIN can delete clients
            authService.requireAdmin(session);
            clientService.deleteClient(id);
            return ResponseEntity.ok(Map.of("message", "Client deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/orders")
    public ResponseEntity<?> getClientOrders(@PathVariable Long id, HttpSession session) {
        try {
            // Only owner or ADMIN can view client orders
            authService.requireOwnerOrAdmin(session, id);
            List<OrderResponse> orders = clientService.getClientOrders(id);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
