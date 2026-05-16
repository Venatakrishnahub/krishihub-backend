package com.krishihub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Long farmerId;

    private Long pilotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentDirection paymentDirection;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private String chequeNumber;
    private LocalDate chequeDate;
    private String bankName;

    @Column(nullable = false)
    private Double amount;

    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Builder.Default
    private boolean cashSubmittedByPilot = false;

    private String cashSubmissionPhoto;
    private Long cashVerifiedByAdmin;
    private LocalDateTime cashVerifiedAt;

    private String transactionRef;
    private String failureReason;

    @Builder.Default
    private Double refundAmount = 0.0;

    private String refundRef;
    private LocalDateTime paidAt;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum PaymentMode {
        RAZORPAY, CASH, UPI, CHEQUE, WALLET
    }

    public enum PaymentDirection {
        FARMER_TO_PLATFORM, PLATFORM_TO_PILOT
    }

    public enum PaymentStatus {
        PENDING, INITIATED, SUCCESS, FAILED, REFUNDED
    }
}
