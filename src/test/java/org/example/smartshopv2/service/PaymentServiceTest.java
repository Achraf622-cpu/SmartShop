package org.example.smartshopv2.service;

import org.example.smartshopv2.dto.PaymentRequest;
import org.example.smartshopv2.dto.PaymentResponse;
import org.example.smartshopv2.entity.Client;
import org.example.smartshopv2.entity.Order;
import org.example.smartshopv2.entity.Payment;
import org.example.smartshopv2.entity.User;
import org.example.smartshopv2.enums.LoyaltyLevel;
import org.example.smartshopv2.enums.OrderStatus;
import org.example.smartshopv2.enums.PaymentStatus;
import org.example.smartshopv2.enums.Role;
import org.example.smartshopv2.repository.ClientRepository;
import org.example.smartshopv2.repository.OrderRepository;
import org.example.smartshopv2.repository.PaymentRepository;
import org.example.smartshopv2.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PaymentService
 * 
 * Tests the complex payment logic including:
 * - Multi-payment support
 * - Cash limit enforcement (20,000 DH)
 * - Payment status transitions
 * - Order montantRestant updates
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("PaymentService Integration Tests")
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Create test user and client
        User user = User.builder()
                .username("testclient")
                .password("pass123")
                .role(Role.CLIENT)
                .build();
        user = userRepository.save(user);

        Client client = Client.builder()
                .user(user)
                .companyName("Test Corp")
                .loyaltyLevel(LoyaltyLevel.BASIC)
                .build();
        client = clientRepository.save(client);

        // Create test order
        testOrder = Order.builder()
                .client(client)
                .subtotalHT(25000.0)
                .discountAmount(0.0)
                .amountAfterDiscount(25000.0)
                .tva(5000.0)
                .totalTTC(30000.0)
                .montantRestant(30000.0)
                .status(OrderStatus.PENDING)
                .build();
        testOrder = orderRepository.save(testOrder);
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        clientRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should add ESPECES payment and mark as ENCAISSE immediately")
    void testAddPayment_Especes_MarkedAsEncaisse() {
        // ARRANGE
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(testOrder.getId());
        request.setMontant(5000.0);
        request.setTypePaiement("ESPECES");
        request.setReference("RECU-001");
        request.setDatePaiement(LocalDateTime.now());

        // ACT
        PaymentResponse response = paymentService.addPayment(request);

        // ASSERT
        assertNotNull(response);
        assertEquals(5000.0, response.getMontant());
        assertEquals("ESPECES", response.getTypePaiement());
        assertEquals(PaymentStatus.ENCAISSE, response.getStatus());
        assertNotNull(response.getDateEncaissement());

        // Verify order montantRestant updated
        Order updatedOrder = orderRepository.findById(testOrder.getId()).get();
        assertEquals(25000.0, updatedOrder.getMontantRestant());
    }

    @Test
    @DisplayName("Should add CHEQUE payment and mark as EN_ATTENTE")
    void testAddPayment_Cheque_MarkedAsEnAttente() {
        // ARRANGE
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(testOrder.getId());
        request.setMontant(12000.0);
        request.setTypePaiement("CHEQUE");
        request.setReference("CHQ-123456");
        request.setBanque("BMCE Bank");
        request.setDatePaiement(LocalDateTime.now());
        request.setDateEcheance(LocalDateTime.now().plusDays(30));

        // ACT
        PaymentResponse response = paymentService.addPayment(request);

        // ASSERT
        assertEquals("CHEQUE", response.getTypePaiement());
        assertEquals(PaymentStatus.EN_ATTENTE, response.getStatus());
        assertNull(response.getDateEncaissement());

        // Verify order montantRestant still updated
        Order updatedOrder = orderRepository.findById(testOrder.getId()).get();
        assertEquals(18000.0, updatedOrder.getMontantRestant());
    }

    @Test
    @DisplayName("Should throw exception when cash payment exceeds 20,000 DH")
    void testAddPayment_CashExceedsLimit_ThrowsException() {
        // ARRANGE
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(testOrder.getId());
        request.setMontant(25000.0); // Exceeds legal limit
        request.setTypePaiement("ESPECES");
        request.setReference("RECU-001");
        request.setDatePaiement(LocalDateTime.now());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.addPayment(request);
        });

        // Verify the exception message mentions the limit
        String message = exception.getMessage().toLowerCase();
        assertTrue(message.contains("20") || message.contains("20,000") || message.contains("cash"),
                "Exception message should mention cash limit: " + exception.getMessage());
    }

    @Test
    @DisplayName("Should encaisser CHEQUE payment successfully")
    void testEncaisserPayment_Cheque_UpdatesStatus() {
        // ARRANGE
        Payment payment = Payment.builder()
                .order(testOrder)
                .numeroPaiement(1)
                .montant(12000.0)
                .typePaiement("CHEQUE")
                .reference("CHQ-123")
                .datePaiement(LocalDateTime.now())
                .status(PaymentStatus.EN_ATTENTE)
                .build();
        payment = paymentRepository.save(payment);

        // ACT
        PaymentResponse response = paymentService.encaisserPayment(payment.getId());

        // ASSERT
        assertEquals(PaymentStatus.ENCAISSE, response.getStatus());
        assertNotNull(response.getDateEncaissement());
    }

    @Test
    @DisplayName("Should reject payment and restore montantRestant")
    void testRejeterPayment_RestoresMontantRestant() {
        // ARRANGE
        // Add a payment first
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(testOrder.getId());
        request.setMontant(5000.0);
        request.setTypePaiement("CHEQUE");
        request.setReference("CHQ-BAD");
        request.setDatePaiement(LocalDateTime.now());

        PaymentResponse addedPayment = paymentService.addPayment(request);

        // Verify montantRestant was reduced
        Order orderAfterPayment = orderRepository.findById(testOrder.getId()).get();
        assertEquals(25000.0, orderAfterPayment.getMontantRestant());

        // ACT - Reject the payment
        PaymentResponse rejectedPayment = paymentService.rejeterPayment(addedPayment.getId());

        // ASSERT
        assertEquals(PaymentStatus.REJETE, rejectedPayment.getStatus());

        // Verify montantRestant restored
        Order orderAfterReject = orderRepository.findById(testOrder.getId()).get();
        assertEquals(30000.0, orderAfterReject.getMontantRestant());
    }

    @Test
    @DisplayName("Should create multiple payments with sequential numbers")
    void testAddPayment_MultiplePayments_SequentialNumbers() {
        // ARRANGE & ACT
        PaymentRequest request1 = new PaymentRequest();
        request1.setOrderId(testOrder.getId());
        request1.setMontant(10000.0);
        request1.setTypePaiement("ESPECES");
        request1.setReference("RECU-001");
        request1.setDatePaiement(LocalDateTime.now());
        PaymentResponse payment1 = paymentService.addPayment(request1);

        PaymentRequest request2 = new PaymentRequest();
        request2.setOrderId(testOrder.getId());
        request2.setMontant(10000.0);
        request2.setTypePaiement("ESPECES");
        request2.setReference("RECU-002");
        request2.setDatePaiement(LocalDateTime.now());
        PaymentResponse payment2 = paymentService.addPayment(request2);

        PaymentRequest request3 = new PaymentRequest();
        request3.setOrderId(testOrder.getId());
        request3.setMontant(10000.0);
        request3.setTypePaiement("ESPECES");
        request3.setReference("RECU-003");
        request3.setDatePaiement(LocalDateTime.now());
        PaymentResponse payment3 = paymentService.addPayment(request3);

        // ASSERT
        assertEquals(1, payment1.getNumeroPaiement());
        assertEquals(2, payment2.getNumeroPaiement());
        assertEquals(3, payment3.getNumeroPaiement());

        // Verify order fully paid
        Order finalOrder = orderRepository.findById(testOrder.getId()).get();
        assertEquals(0.0, finalOrder.getMontantRestant());
    }
}
