package com.krishihub.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pilots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pilot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String phone;

    private String email;

    @Column(unique = true, nullable = false)
    private String aadhaarNumber;

    @Builder.Default
    private boolean aadhaarVerified = false;

    private String aadhaarFrontPhoto;
    private String aadhaarBackPhoto;
    private String profilePhoto;
    private String village;
    private String mandal;
    private String district;

    @Builder.Default
    private String state = "Andhra Pradesh";

    private String dronePilotLicense;
    private LocalDate licenseExpiryDate;
    private String licenseDocument;
    private String dgcaUinNumber;

    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String upiId;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer totalRatings = 0;

    @Builder.Default
    private Double totalAcresSprayed = 0.0;

    @Builder.Default
    private Integer totalBookingsCompleted = 0;

    @Builder.Default
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Builder.Default
    private String status = "pending";

    @Builder.Default
    private boolean isOnline = false;

    private Double currentLatitude;
    private Double currentLongitude;

    @Builder.Default
    private Integer serviceAreaRadiusKm = 30;

    private String passwordHash;
    private String otpCode;
    private LocalDateTime otpExpiresAt;
    private String fcmToken;

    @Builder.Default
    private String preferredLanguage = "te";

    private Long registeredByAdmin;
    private String registrationNotes;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
