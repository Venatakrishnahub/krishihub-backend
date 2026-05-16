package com.krishihub.repository;

import com.krishihub.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRazorpayOrderId(String orderId);
    Page<Payment> findByFarmerIdOrderByCreatedAtDesc(Long farmerId, Pageable pageable);
    List<Payment> findByBookingId(Long bookingId);
}
