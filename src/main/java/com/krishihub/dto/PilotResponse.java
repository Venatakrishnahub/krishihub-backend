package com.krishihub.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PilotResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String district;
    private String status;
    private Double averageRating;
    private Double totalAcresSprayed;
    private boolean isOnline;
}
