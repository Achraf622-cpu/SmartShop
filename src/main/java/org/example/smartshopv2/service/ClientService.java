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
import org.example.smartshopv2.mapper.ClientMapper;
import org.example.smartshopv2.mapper.OrderMapper;
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
    private final ClientMapper clientMapper;
    private final OrderMapper orderMapper;

    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .role(Role.CLIENT)
                .build();

        Client client = Client.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .contactName(request.getContactName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        user.setClient(client);

        User savedUser = userRepository.save(user);
        return clientMapper.toResponse(savedUser.getClient());
    }

    public ClientResponse getClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return clientMapper.toResponse(client);
    }

    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
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
        return clientMapper.toResponse(updated);
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
                .map(orderMapper::toResponse)
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
}
