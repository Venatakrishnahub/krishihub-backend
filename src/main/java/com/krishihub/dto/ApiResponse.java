package com.krishihub.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
    public static ApiResponse success(String msg) {
        return ApiResponse.builder().success(true).message(msg).build();
    }
    public static ApiResponse error(String msg) {
        return ApiResponse.builder().success(false).message(msg).build();
    }
}
