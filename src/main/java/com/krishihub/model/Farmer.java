package com.krishihub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String phone;

    private String alternatePhone;

    @Column(unique = true)
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

    private String pincode;
    private Double latitude;
    private Double longitude;

    @Builder.Default
    private String preferredLanguage = "te";

    @Builder.Default
    private Double totalAcresSprayed = 0.0;

    @Builder.Default
    private Integer totalBookings = 0;

    @Builder.Default
    private Double walletBalance = 0.0;

    private String referralCode;

    @Builder.Default
    private boolean isActive = true;

    @Builder.Default
    private boolean isVerified = false;

    private String otpCode;
    private LocalDateTime otpExpiresAt;
    private String fcmToken;
    private String deviceType;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
