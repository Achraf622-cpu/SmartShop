package org.example.smartshopv2.repository;

import org.example.smartshopv2.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderIdOrderByNumeroPaiementAsc(Long orderId);
}
