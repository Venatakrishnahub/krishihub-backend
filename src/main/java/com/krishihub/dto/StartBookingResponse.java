package com.krishihub.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StartBookingResponse {
    private String otp;
    private String message;
}
