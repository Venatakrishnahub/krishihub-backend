package com.krishihub.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingEstimateResponse {
    private Double servicePrice;
    private Double platformFee;
    private Double pilotBonus;
    private Double discountAmount;
    private Double totalAmount;
    private String couponApplied;
}
