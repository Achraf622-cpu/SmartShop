package org.example.smartshopv2.service;

import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.dto.*;
import org.example.smartshopv2.entity.*;
import org.example.smartshopv2.enums.LoyaltyLevel;
import org.example.smartshopv2.enums.OrderStatus;
import org.example.smartshopv2.repository.ClientRepository;
import org.example.smartshopv2.repository.OrderRepository;
import org.example.smartshopv2.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final ClientService clientService;
    
    private static final double TVA_RATE = 0.20;
    
    @Transactional
    public OrderResponse createOrder(Long clientId, OrderRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        Order order = new Order();
        order.setClient(client);
        order.setPromoCode(request.getPromoCode());
        
        double subtotalHT = 0.0;
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));
            
            if (product.getDeleted()) {
                throw new RuntimeException("Product is no longer available: " + product.getName());
            }
            
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceHT(product.getPriceHT());
            orderItem.setSubtotal(product.getPriceHT() * itemRequest.getQuantity());
            
            order.getItems().add(orderItem);
            subtotalHT += orderItem.getSubtotal();
        }
        
        order.setSubtotalHT(subtotalHT);
        
        double discountAmount = calculateDiscount(client.getLoyaltyLevel(), subtotalHT, request.getPromoCode());
        order.setDiscountAmount(discountAmount);
        
        double amountAfterDiscount = subtotalHT - discountAmount;
        order.setAmountAfterDiscount(amountAfterDiscount);
        
        double tva = amountAfterDiscount * TVA_RATE;
        order.setTva(tva);
        
        double totalTTC = amountAfterDiscount + tva;
        order.setTotalTTC(totalTTC);
        order.setMontantRestant(totalTTC);
        
        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }
    
    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be confirmed");
        }
        
        // Check if order is fully paid
        if (order.getMontantRestant() > 0) {
            throw new RuntimeException("Order must be fully paid before confirmation. Remaining: " + 
                    order.getMontantRestant() + " DH");
        }
        
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            
            if (product.getStockQuantity() < item.getQuantity()) {
                order.setStatus(OrderStatus.REJECTED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }
        
        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        
        Client client = order.getClient();
        client.setTotalOrders(client.getTotalOrders() + 1);
        client.setTotalSpent(client.getTotalSpent() + order.getTotalTTC());
        
        if (client.getFirstOrderDate() == null) {
            client.setFirstOrderDate(order.getCreatedAt());
        }
        client.setLastOrderDate(order.getCreatedAt());
        
        clientService.updateLoyaltyLevel(client);
        clientRepository.save(client);
        
        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }
    
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            throw new RuntimeException("Confirmed orders cannot be canceled");
        }
        
        order.setStatus(OrderStatus.CANCELED);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }
    
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }
    
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private double calculateDiscount(LoyaltyLevel level, double subtotalHT, String promoCode) {
        double discountPercent = 0.0;
        
        switch (level) {
            case SILVER:
                if (subtotalHT >= 500) {
                    discountPercent = 0.05;
                }
                break;
            case GOLD:
                if (subtotalHT >= 800) {
                    discountPercent = 0.10;
                }
                break;
            case PLATINUM:
                if (subtotalHT >= 1200) {
                    discountPercent = 0.15;
                }
                break;
            case BASIC:
            default:
                // BASIC level has no discount
                break;
        }
        
        if (promoCode != null && promoCode.startsWith("PROMO-")) {
            discountPercent += 0.05;
        }
        
        return subtotalHT * discountPercent;
    }
    
    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setClientId(order.getClient().getId());
        response.setClientName(order.getClient().getCompanyName());
        
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);
        
        response.setSubtotalHT(order.getSubtotalHT());
        response.setPromoCode(order.getPromoCode());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setAmountAfterDiscount(order.getAmountAfterDiscount());
        response.setTva(order.getTva());
        response.setTotalTTC(order.getTotalTTC());
        response.setMontantRestant(order.getMontantRestant());
        
        List<PaymentResponse> paymentResponses = order.getPayments().stream()
                .map(this::mapPaymentToResponse)
                .collect(Collectors.toList());
        response.setPayments(paymentResponses);
        
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        
        return response;
    }
    
    private OrderItemResponse mapItemToResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setQuantity(item.getQuantity());
        response.setPriceHT(item.getPriceHT());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
    
    private PaymentResponse mapPaymentToResponse(Payment payment) {
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
