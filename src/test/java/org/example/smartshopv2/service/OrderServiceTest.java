package org.example.smartshopv2.service;

import org.example.smartshopv2.dto.OrderItemRequest;
import org.example.smartshopv2.dto.OrderRequest;
import org.example.smartshopv2.dto.OrderResponse;
import org.example.smartshopv2.entity.Client;
import org.example.smartshopv2.entity.Product;
import org.example.smartshopv2.entity.User;
import org.example.smartshopv2.enums.LoyaltyLevel;
import org.example.smartshopv2.enums.OrderStatus;
import org.example.smartshopv2.enums.Role;
import org.example.smartshopv2.repository.ClientRepository;
import org.example.smartshopv2.repository.OrderRepository;
import org.example.smartshopv2.repository.ProductRepository;
import org.example.smartshopv2.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OrderService
 * 
 * These tests use REAL database and ALL components!
 * Slower but tests real behavior.
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("OrderService Integration Tests")
class OrderServiceTest {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private Client testClient;
    private Product testProduct1;
    private Product testProduct2;
    
    @BeforeEach
    void setUp() {
        // Create test user
        User user = new User();
        user.setUsername("testclient");
        user.setPassword("pass123");
        user.setRole(Role.CLIENT);
        user = userRepository.save(user);
        
        // Create test client (BASIC level)
        testClient = new Client();
        testClient.setUser(user);
        testClient.setCompanyName("Test Corporation");
        testClient.setLoyaltyLevel(LoyaltyLevel.BASIC);
        testClient.setTotalOrders(0);
        testClient.setTotalSpent(0.0);
        testClient = clientRepository.save(testClient);
        
        // Create test products
        testProduct1 = new Product();
        testProduct1.setName("Laptop Dell");
        testProduct1.setPriceHT(1000.0);
        testProduct1.setStockQuantity(10);
        testProduct1 = productRepository.save(testProduct1);
        
        testProduct2 = new Product();
        testProduct2.setName("Mouse Logitech");
        testProduct2.setPriceHT(50.0);
        testProduct2.setStockQuantity(20);
        testProduct2 = productRepository.save(testProduct2);
    }
    
    @AfterEach
    void tearDown() {
        // Clean up in reverse order (foreign keys!)
        orderRepository.deleteAll();
        clientRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Should create order with correct totals")
    void testCreateOrder_ValidRequest_CalculatesCorrectly() {
        // ARRANGE
        OrderRequest request = new OrderRequest();
        
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getId());
        item1.setQuantity(2); // 2 laptops = 2000 DH
        
        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(testProduct2.getId());
        item2.setQuantity(5); // 5 mice = 250 DH
        
        request.setItems(List.of(item1, item2));
        
        // ACT
        OrderResponse response = orderService.createOrder(testClient.getId(), request);
        
        // ASSERT
        assertNotNull(response);
        assertEquals(2250.0, response.getSubtotalHT()); // 2000 + 250
        assertEquals(0.0, response.getDiscountAmount()); // BASIC level = no discount
        assertEquals(2250.0, response.getAmountAfterDiscount());
        assertEquals(450.0, response.getTva()); // 20% of 2250
        assertEquals(2700.0, response.getTotalTTC()); // 2250 + 450
        assertEquals(2700.0, response.getMontantRestant()); // Not paid yet
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(2, response.getItems().size());
    }
    
    @Test
    @DisplayName("Should apply SILVER discount when eligible")
    void testCreateOrder_SilverLevel_AppliesDiscount() {
        // ARRANGE
        // Upgrade client to SILVER
        testClient.setLoyaltyLevel(LoyaltyLevel.SILVER);
        testClient = clientRepository.save(testClient);
        
        OrderRequest request = new OrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(testProduct1.getId());
        item.setQuantity(1); // 1000 DH (>= 500, eligible for discount)
        request.setItems(List.of(item));
        
        // ACT
        OrderResponse response = orderService.createOrder(testClient.getId(), request);
        
        // ASSERT
        assertEquals(1000.0, response.getSubtotalHT());
        assertEquals(50.0, response.getDiscountAmount()); // 5% of 1000
        assertEquals(950.0, response.getAmountAfterDiscount());
        assertEquals(190.0, response.getTva()); // 20% of 950
        assertEquals(1140.0, response.getTotalTTC());
    }
    
    @Test
    @DisplayName("Should apply GOLD discount when eligible")
    void testCreateOrder_GoldLevel_AppliesDiscount() {
        // ARRANGE
        testClient.setLoyaltyLevel(LoyaltyLevel.GOLD);
        testClient = clientRepository.save(testClient);
        
        OrderRequest request = new OrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(testProduct1.getId());
        item.setQuantity(1); // 1000 DH (>= 800, eligible for 10% discount)
        request.setItems(List.of(item));
        
        // ACT
        OrderResponse response = orderService.createOrder(testClient.getId(), request);
        
        // ASSERT
        assertEquals(1000.0, response.getSubtotalHT());
        assertEquals(100.0, response.getDiscountAmount()); // 10% of 1000
        assertEquals(900.0, response.getAmountAfterDiscount());
    }
    
    @Test
    @DisplayName("Should apply promo code discount")
    void testCreateOrder_PromoCode_AppliesAdditionalDiscount() {
        // ARRANGE
        testClient.setLoyaltyLevel(LoyaltyLevel.SILVER);
        testClient = clientRepository.save(testClient);
        
        OrderRequest request = new OrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(testProduct1.getId());
        item.setQuantity(1); // 1000 DH
        request.setItems(List.of(item));
        request.setPromoCode("PROMO-2024");
        
        // ACT
        OrderResponse response = orderService.createOrder(testClient.getId(), request);
        
        // ASSERT
        // SILVER (5%) + PROMO (5%) = 10% total
        assertEquals(1000.0, response.getSubtotalHT());
        assertEquals(100.0, response.getDiscountAmount()); // 10% of 1000
        assertEquals(900.0, response.getAmountAfterDiscount());
    }
    
    @Test
    @DisplayName("Should throw exception when client not found")
    void testCreateOrder_ClientNotFound_ThrowsException() {
        // ARRANGE
        OrderRequest request = new OrderRequest();
        
        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(999L, request);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when product not found")
    void testCreateOrder_ProductNotFound_ThrowsException() {
        // ARRANGE
        OrderRequest request = new OrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(999L); // Non-existent product
        item.setQuantity(1);
        request.setItems(List.of(item));
        
        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(testClient.getId(), request);
        });
    }
    
    @Test
    @DisplayName("Should throw exception for insufficient stock on confirm")
    void testConfirmOrder_InsufficientStock_ThrowsException() {
        // ARRANGE
        // Create order with 5 laptops (stock is 10, so order creation succeeds)
        OrderRequest request = new OrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(testProduct1.getId());
        item.setQuantity(5);
        request.setItems(List.of(item));
        
        OrderResponse createdOrder = orderService.createOrder(testClient.getId(), request);
        
        // Now reduce stock to 2 (less than ordered 5)
        testProduct1.setStockQuantity(2);
        productRepository.save(testProduct1);
        
        // Set montant restant to 0 (fully paid)
        var order = orderRepository.findById(createdOrder.getId()).get();
        order.setMontantRestant(0.0);
        orderRepository.save(order);
        
        // ACT & ASSERT - Should fail because stock (2) < ordered (5)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.confirmOrder(createdOrder.getId());
        });
        
        assertTrue(exception.getMessage().contains("Insufficient stock") || 
                   exception.getMessage().contains("stock"),
                   "Exception should mention stock issue: " + exception.getMessage());
    }
    
    @Test
    @DisplayName("Should not confirm order if not fully paid")
    void testConfirmOrder_NotPaid_ThrowsException() {
        // ARRANGE
        OrderRequest request = new OrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(testProduct1.getId());
        item.setQuantity(1);
        request.setItems(List.of(item));
        
        OrderResponse createdOrder = orderService.createOrder(testClient.getId(), request);
        
        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.confirmOrder(createdOrder.getId());
        });
        
        assertTrue(exception.getMessage().contains("must be fully paid"));
    }
}
