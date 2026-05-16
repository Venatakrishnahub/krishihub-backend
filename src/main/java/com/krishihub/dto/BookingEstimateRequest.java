package com.krishihub.dto;
import lombok.Data;
@Data
public class BookingEstimateRequest {
    private Long serviceTypeId;
    private Double areaAcres;
    private String couponCode;
    private Double pilotBonus;
}
