package com.krishihub.controller;

import com.krishihub.dto.*;
import com.krishihub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/farmer/send-otp")
    public ResponseEntity<ApiResponse> farmerSendOtp(
            @Valid @RequestBody SendOtpRequest req) {
        authService.sendOtpToFarmer(req.getPhone());
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully"));
    }

    @PostMapping("/farmer/verify-otp")
    public ResponseEntity<AuthResponse> farmerVerifyOtp(
            @Valid @RequestBody VerifyOtpRequest req) {
        return ResponseEntity.ok(authService.verifyFarmerOtp(req));
    }

    @PutMapping("/farmer/complete-profile")
    public ResponseEntity<ApiResponse> completeFarmerProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody FarmerProfileRequest req) {
        authService.completeFarmerProfile(token, req);
        return ResponseEntity.ok(ApiResponse.success("Profile updated"));
    }

    @PostMapping("/pilot/send-otp")
    public ResponseEntity<ApiResponse> pilotSendOtp(
            @Valid @RequestBody SendOtpRequest req) {
        authService.sendOtpToPilot(req.getPhone());
        return ResponseEntity.ok(ApiResponse.success("OTP sent"));
    }

    @PostMapping("/pilot/verify-otp")
    public ResponseEntity<AuthResponse> pilotVerifyOtp(
            @Valid @RequestBody VerifyOtpRequest req) {
        return ResponseEntity.ok(authService.verifyPilotOtp(req));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(
            @Valid @RequestBody AdminLoginRequest req) {
        return ResponseEntity.ok(authService.adminLogin(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(authService.refreshToken(req.getRefreshToken()));
    }
}
