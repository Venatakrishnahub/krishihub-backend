package com.krishihub.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String role;
    private boolean isNewUser;
    private Object user;
}
