package com.krishihub.dto;
import lombok.Data;
@Data
public class CashVerifyRequest {
    private boolean approved;
    private String notes;
}
