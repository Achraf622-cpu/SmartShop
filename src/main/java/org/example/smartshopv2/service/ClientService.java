package org.example.smartshopv2.service;

import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.dto.ClientRequest;
import org.example.smartshopv2.dto.ClientResponse;
import org.example.smartshopv2.dto.OrderResponse;
import org.example.smartshopv2.entity.Client;
import org.example.smartshopv2.entity.Order;
import org.example.smartshopv2.entity.User;
import org.example.smartshopv2.enums.LoyaltyLevel;
import org.example.smartshopv2.enums.Role;
import org.example.smartshopv2.repository.ClientRepository;
import org.example.smartshopv2.repository.OrderRepository;
import org.example.smartshopv2.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {
    
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    
    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole(Role.CLIENT);
        
        Client client = new Client();
        client.setUser(user);
        client.setCompanyName(request.getCompanyName());
        client.setContactName(request.getContactName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());
        
        user.setClient(client);
        
        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser.getClient());
    }
    
    public ClientResponse getClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return mapToResponse(client);
    }
    
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        client.setCompanyName(request.getCompanyName());
        client.setContactName(request.getContactName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());
        
        Client updated = clientRepository.save(client);
        return mapToResponse(updated);
    }
    
    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        userRepository.delete(client.getUser());
    }
    
    public List<OrderResponse> getClientOrders(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found");
        }
        
        List<Order> orders = orderRepository.findByClientIdOrderByCreatedAtDesc(clientId);
        return orders.stream()
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
    }
    
    public void updateLoyaltyLevel(Client client) {
        Integer totalOrders = client.getTotalOrders();
        Double totalSpent = client.getTotalSpent();
        
        if (totalOrders >= 20 || totalSpent >= 15000) {
            client.setLoyaltyLevel(LoyaltyLevel.PLATINUM);
        } else if (totalOrders >= 10 || totalSpent >= 5000) {
            client.setLoyaltyLevel(LoyaltyLevel.GOLD);
        } else if (totalOrders >= 3 || totalSpent >= 1000) {
            client.setLoyaltyLevel(LoyaltyLevel.SILVER);
        } else {
            client.setLoyaltyLevel(LoyaltyLevel.BASIC);
        }
    }
    
    private ClientResponse mapToResponse(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setCompanyName(client.getCompanyName());
        response.setContactName(client.getContactName());
        response.setEmail(client.getEmail());
        response.setPhone(client.getPhone());
        response.setAddress(client.getAddress());
        response.setLoyaltyLevel(client.getLoyaltyLevel());
        response.setTotalOrders(client.getTotalOrders());
        response.setTotalSpent(client.getTotalSpent());
        response.setFirstOrderDate(client.getFirstOrderDate());
        response.setLastOrderDate(client.getLastOrderDate());
        response.setCreatedAt(client.getCreatedAt());
        return response;
    }
    
    private OrderResponse mapOrderToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setClientId(order.getClient().getId());
        response.setClientName(order.getClient().getCompanyName());
        response.setSubtotalHT(order.getSubtotalHT());
        response.setPromoCode(order.getPromoCode());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setAmountAfterDiscount(order.getAmountAfterDiscount());
        response.setTva(order.getTva());
        response.setTotalTTC(order.getTotalTTC());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }
}
