package com.krishihub.service;

import com.krishihub.dto.*;
import com.krishihub.model.Booking;
import com.krishihub.model.Payment;
import com.krishihub.repository.BookingRepository;
import com.krishihub.repository.PaymentRepository;
import com.krishihub.security.JwtUtil;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final BookingRepository bookingRepo;
    private final PaymentRepository paymentRepo;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;

    @Value("${razorpay.key-id}")     private String razorpayKeyId;
    @Value("${razorpay.key-secret}") private String razorpaySecret;

    // ── CREATE ORDER ──────────────────────────────────────────────
    @Transactional
    public RazorpayOrderResponse createRazorpayOrder(String rawToken, Long bookingId) {
        Long farmerId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        long amountPaise = (long) (booking.getTotalAmount().doubleValue() * 100);

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpaySecret);
            JSONObject orderReq = new JSONObject();
            orderReq.put("amount", amountPaise);
            orderReq.put("currency", "INR");
            orderReq.put("receipt", booking.getBookingNumber() != null
                    ? booking.getBookingNumber() : "KH" + bookingId);
            Order order = client.orders.create(orderReq);

            Payment payment = Payment.builder()
                    .bookingId(bookingId)
                    .farmerId(farmerId)
                    .paymentMode(Payment.PaymentMode.RAZORPAY)
                    .paymentDirection(Payment.PaymentDirection.FARMER_TO_PLATFORM)
                    .razorpayOrderId(order.get("id"))
                    .amount(booking.getTotalAmount().doubleValue())
                    .build();
            paymentRepo.save(payment);

            String farmerName = booking.getFarmer() != null
                    ? booking.getFarmer().getFullName() : "";

            return RazorpayOrderResponse.builder()
                    .orderId(order.get("id"))
                    .amount(amountPaise)
                    .currency("INR")
                    .keyId(razorpayKeyId)
                    .bookingNumber(booking.getBookingNumber())
                    .farmerName(farmerName)
                    .build();

        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create payment order: " + e.getMessage());
        }
    }

    // ── VERIFY PAYMENT ────────────────────────────────────────────
    @Transactional
    public boolean verifyRazorpayPayment(String rawToken, RazorpayVerifyRequest req) {
        try {
            String expected = hmacSha256(
                    req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId(),
                    razorpaySecret);

            if (!expected.equals(req.getRazorpaySignature())) {
                log.warn("Payment signature mismatch for order {}", req.getRazorpayOrderId());
                return false;
            }

            Payment payment = paymentRepo.findByRazorpayOrderId(req.getRazorpayOrderId())
                    .orElseThrow(() -> new RuntimeException("Payment record not found"));

            payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
            payment.setRazorpaySignature(req.getRazorpaySignature());
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepo.save(payment);

            Booking booking = bookingRepo.findById(payment.getBookingId()).orElseThrow();
            if (booking.getFarmer() != null) {
                notificationService.notifyPaymentSuccess(
                        booking.getFarmer(), booking, payment.getAmount());
            }
            return true;
        } catch (Exception e) {
            log.error("Payment verification error: {}", e.getMessage());
            return false;
        }
    }

    // ── WEBHOOK ───────────────────────────────────────────────────
    public void handleRazorpayWebhook(String payload, String signature) {
        try {
            String expected = hmacSha256(payload, razorpaySecret);
            if (!expected.equals(signature)) {
                log.warn("Invalid webhook signature");
                return;
            }
            JSONObject event = new JSONObject(payload);
            log.info("Razorpay webhook: {}", event.getString("event"));
        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
        }
    }

    // ── ADMIN: verify cash ────────────────────────────────────────
    @Transactional
    public void adminVerifyCashPayment(String rawToken, Long paymentId, CashVerifyRequest req) {
        Long adminId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setCashVerifiedByAdmin(adminId);
        payment.setCashVerifiedAt(LocalDateTime.now());
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        paymentRepo.save(payment);
    }

    // ── Farmer payment history ────────────────────────────────────
    public PagedResponse<PaymentResponse> getFarmerPaymentHistory(
            String rawToken, int page, int size) {
        Long farmerId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        var payments = paymentRepo.findByFarmerIdOrderByCreatedAtDesc(
                farmerId, PageRequest.of(page, size));
        List<PaymentResponse> content = payments.getContent().stream()
                .map(p -> PaymentResponse.builder()
                        .id(p.getId())
                        .paymentMode(p.getPaymentMode().name())
                        .amount(p.getAmount())
                        .status(p.getStatus().name())
                        .paidAt(p.getPaidAt() != null ? p.getPaidAt().toString() : null)
                        .build())
                .collect(Collectors.toList());
        return PagedResponse.<PaymentResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(payments.getTotalElements())
                .totalPages(payments.getTotalPages())
                .last(payments.isLast()).build();
    }

    // ── HMAC-SHA256 ───────────────────────────────────────────────
    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
