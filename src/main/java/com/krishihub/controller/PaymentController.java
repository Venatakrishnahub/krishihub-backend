package com.krishihub.controller;

import com.krishihub.dto.*;
import com.krishihub.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<RazorpayOrderResponse> createOrder(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(token, req.getBookingId()));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse> verify(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody RazorpayVerifyRequest req) {
        boolean ok = paymentService.verifyRazorpayPayment(token, req);
        return ok
                ? ResponseEntity.ok(ApiResponse.success("Payment successful!"))
                : ResponseEntity.badRequest().body(ApiResponse.error("Payment verification failed"));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestHeader("X-Razorpay-Signature") String sig,
            @RequestBody String payload) {
        paymentService.handleRazorpayWebhook(payload, sig);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<PagedResponse<PaymentResponse>> myHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(paymentService.getFarmerPaymentHistory(token, page, size));
    }

    @PutMapping("/{id}/verify-cash")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> verifyCash(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody CashVerifyRequest req) {
        paymentService.adminVerifyCashPayment(token, id, req);
        return ResponseEntity.ok(ApiResponse.success("Cash payment verified"));
    }
}
