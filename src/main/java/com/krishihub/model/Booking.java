package com.krishihub.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bookingNumber;

    @Column(nullable = false)
    private Long farmerId;

    private Long pilotId;
    private Long droneId;
    private Long serviceTypeId;
    private Long farmFieldId;

    // Field details
    @Column(nullable = false)
    private String fieldVillage;
    private String fieldMandal;
    private String fieldDistrict;
    private Double fieldLatitude;
    private Double fieldLongitude;
    private Double areaAcres;
    private String cropType;
    private String surveyNumber;

    // Schedule
    private LocalDate scheduledDate;
    private String shift;
    private String preferredTimeNote;

    // Mixer
    @Builder.Default
    private boolean requiresMixer = false;
    private String mixerName;
    private String mixerPhone;

    @Builder.Default
    private BigDecimal mixerFee = BigDecimal.ZERO;

    // Chemical
    private String chemicalName;
    private Double chemicalQuantityLiters;
    private Double waterQuantityLiters;
    private String mixingRatio;

    @Column(length = 1000)
    private String specialInstructions;

    // Pricing
    private BigDecimal servicePricePerAcre;
    private BigDecimal totalServicePrice;
    private BigDecimal platformFee;

    @Builder.Default
    private BigDecimal pilotBonus = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String couponCode;
    private BigDecimal totalAmount;
    private BigDecimal pilotEarning;

    @Builder.Default
    private String status = "pending";

    // Completion
    private String otpForCompletion;

    @Builder.Default
    private boolean otpVerified = false;

    private LocalDateTime otpVerifiedAt;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Double actualAcresSprayed;

    // Cancellation
    private String cancelledBy;
    private String cancellationReason;
    private LocalDateTime cancelledAt;

    // Reschedule
    private LocalDate rescheduledDate;
    private String rescheduledShift;
    private String rescheduleReason;
    private String rescheduledBy;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // Relationships (lazy, read-only)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmerId", insertable = false, updatable = false)
    private Farmer farmer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pilotId", insertable = false, updatable = false)
    private Pilot pilot;

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
