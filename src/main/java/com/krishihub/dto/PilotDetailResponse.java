package com.krishihub.dto;
import lombok.*;
import java.time.LocalDate;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PilotDetailResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String aadhaarNumber;
    private boolean aadhaarVerified;
    private String dronePilotLicense;
    private LocalDate licenseExpiryDate;
    private String district;
    private String status;
    private Double averageRating;
    private Integer totalRatings;
    private Double totalAcresSprayed;
    private Integer totalBookingsCompleted;
    private Double totalEarnings;
    private boolean isOnline;
}
