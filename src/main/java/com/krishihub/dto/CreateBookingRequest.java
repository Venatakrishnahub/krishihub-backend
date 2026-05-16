package com.krishihub.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
@Data
public class CreateBookingRequest {
    @NotNull private Long serviceTypeId;
    @NotBlank private String fieldVillage;
    private String fieldMandal;
    private String fieldDistrict;
    @NotNull private Double fieldLatitude;
    @NotNull private Double fieldLongitude;
    @NotNull @Positive private Double areaAcres;
    private String cropType;
    private String surveyNumber;
    @NotNull private LocalDate scheduledDate;
    @NotBlank private String shift;
    private boolean requiresMixer;
    private String mixerName;
    private String mixerPhone;
    private String chemicalName;
    private Double chemicalQuantityLiters;
    private Double waterQuantityLiters;
    private String specialInstructions;
    private Double pilotBonus;
    private String couponCode;
}
