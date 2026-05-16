package com.krishihub.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String bookingNumber;
    private String status;
    private String serviceName;
    private Double areaAcres;
    private String fieldVillage;
    private String fieldDistrict;
    private String scheduledDate;
    private String shift;
    private Double totalAmount;
    private Double pilotEarning;
    private String pilotName;
    private String pilotPhone;
    private String otpForCompletion;
    private boolean otpVerified;
    private String createdAt;
}
