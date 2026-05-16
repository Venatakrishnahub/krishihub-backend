package com.krishihub.service;

import com.krishihub.dto.*;
import com.krishihub.model.Admin;
import com.krishihub.model.Farmer;
import com.krishihub.model.Pilot;
import com.krishihub.repository.AdminRepository;
import com.krishihub.repository.FarmerRepository;
import com.krishihub.repository.PilotRepository;
import com.krishihub.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final FarmerRepository farmerRepo;
    private final PilotRepository pilotRepo;
    private final AdminRepository adminRepo;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    @Value("${krishihub.otp-dev-bypass:false}")
    private boolean devBypass;

    @Value("${krishihub.dev-otp:123456}")
    private String devOtp;

    // ── FARMER ────────────────────────────────────────────────────

    @Transactional
    public void sendOtpToFarmer(String phone) {
        String otp = devBypass ? devOtp : randomOtp();
        Farmer farmer = farmerRepo.findByPhone(phone).orElseGet(() -> {
            Farmer f = new Farmer();
            f.setPhone(phone);
            f.setFullName("");
            return f;
        });
        farmer.setOtpCode(otp);
        farmer.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        farmerRepo.save(farmer);
        if (!devBypass) {
            otpService.sendSms(phone, "KrishiHub OTP: " + otp + ". Valid 10 mins. Do not share.");
        } else {
            log.info("DEV OTP for {}: {}", phone, otp);
        }
    }

    @Transactional
    public AuthResponse verifyFarmerOtp(VerifyOtpRequest req) {
        Farmer farmer = farmerRepo.findByPhone(req.getPhone())
                .orElseThrow(() -> new RuntimeException("Phone not found. Please request OTP first."));

        if (farmer.getOtpExpiresAt() == null || LocalDateTime.now().isAfter(farmer.getOtpExpiresAt())) {
            throw new RuntimeException("OTP expired. Please request a new one.");
        }
        if (!farmer.getOtpCode().equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        farmer.setOtpCode(null);
        farmer.setOtpExpiresAt(null);
        if (req.getFcmToken() != null) farmer.setFcmToken(req.getFcmToken());
        if (req.getDeviceType() != null) farmer.setDeviceType(req.getDeviceType());
        farmerRepo.save(farmer);

        boolean isNew = farmer.getFullName() == null || farmer.getFullName().isBlank();
        String token = jwtUtil.generateToken(farmer.getPhone(), "FARMER", farmer.getId());
        String refresh = jwtUtil.generateRefreshToken(farmer.getPhone());

        return AuthResponse.builder()
                .token(token).refreshToken(refresh)
                .role("farmer").isNewUser(isNew)
                .user(toFarmerMap(farmer))
                .build();
    }

    @Transactional
    public void completeFarmerProfile(String rawToken, FarmerProfileRequest req) {
        String phone = jwtUtil.extractSubject(rawToken.replace("Bearer ", ""));
        Farmer farmer = farmerRepo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
        if (req.getFullName() != null)         farmer.setFullName(req.getFullName());
        if (req.getAadhaarNumber() != null)    farmer.setAadhaarNumber(req.getAadhaarNumber());
        if (req.getVillage() != null)          farmer.setVillage(req.getVillage());
        if (req.getMandal() != null)           farmer.setMandal(req.getMandal());
        if (req.getDistrict() != null)         farmer.setDistrict(req.getDistrict());
        if (req.getPreferredLanguage() != null) farmer.setPreferredLanguage(req.getPreferredLanguage());
        farmerRepo.save(farmer);
    }

    // ── PILOT ─────────────────────────────────────────────────────

    @Transactional
    public void sendOtpToPilot(String phone) {
        Pilot pilot = pilotRepo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Pilot not registered. Please contact admin."));
        if (!"active".equals(pilot.getStatus())) {
            throw new RuntimeException("Account not active. Contact admin.");
        }
        String otp = devBypass ? devOtp : randomOtp();
        pilot.setOtpCode(otp);
        pilot.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        pilotRepo.save(pilot);
        if (!devBypass) {
            otpService.sendSms(phone, "KrishiHub OTP: " + otp + ". Valid 10 mins.");
        } else {
            log.info("DEV OTP for pilot {}: {}", phone, otp);
        }
    }

    @Transactional
    public AuthResponse verifyPilotOtp(VerifyOtpRequest req) {
        Pilot pilot = pilotRepo.findByPhone(req.getPhone())
                .orElseThrow(() -> new RuntimeException("Pilot not found"));

        if (pilot.getOtpExpiresAt() == null || LocalDateTime.now().isAfter(pilot.getOtpExpiresAt())) {
            throw new RuntimeException("OTP expired");
        }
        if (!pilot.getOtpCode().equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        pilot.setOtpCode(null);
        pilot.setOtpExpiresAt(null);
        if (req.getFcmToken() != null) pilot.setFcmToken(req.getFcmToken());
        pilotRepo.save(pilot);

        String token = jwtUtil.generateToken(pilot.getPhone(), "PILOT", pilot.getId());
        String refresh = jwtUtil.generateRefreshToken(pilot.getPhone());

        return AuthResponse.builder()
                .token(token).refreshToken(refresh)
                .role("pilot").isNewUser(false)
                .user(toPilotMap(pilot))
                .build();
    }

    // ── ADMIN ─────────────────────────────────────────────────────

    @Transactional
    public AuthResponse adminLogin(AdminLoginRequest req) {
        Admin admin = adminRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));


        if (!passwordEncoder.matches(req.getPassword(), admin.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        if (!admin.isActive()) {
            throw new RuntimeException("Account disabled");
        }
        admin.setLastLogin(LocalDateTime.now());
        adminRepo.save(admin);

        String token = jwtUtil.generateToken(admin.getEmail(), "ADMIN", admin.getId());
        String refresh = jwtUtil.generateRefreshToken(admin.getEmail());

        return AuthResponse.builder()
                .token(token).refreshToken(refresh)
                .role("admin").isNewUser(false)
                .user(Map.of("id", admin.getId(), "name", admin.getName(), "email", admin.getEmail()))
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String subject = jwtUtil.extractSubject(refreshToken);
        String role    = jwtUtil.extractRole(refreshToken);
        Long   userId  = jwtUtil.extractUserId(refreshToken);
        String newToken = jwtUtil.generateToken(subject, role, userId);
        return AuthResponse.builder().token(newToken).refreshToken(refreshToken).build();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String randomOtp() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    private Map<String, Object> toFarmerMap(Farmer f) {
        return Map.of(
                "id",               f.getId(),
                "fullName",         f.getFullName() != null ? f.getFullName() : "",
                "phone",            f.getPhone(),
                "district",         f.getDistrict() != null ? f.getDistrict() : "",
                "preferredLanguage", f.getPreferredLanguage(),
                "aadhaarVerified",  f.isAadhaarVerified()
        );
    }

    private Map<String, Object> toPilotMap(Pilot p) {
        return Map.of(
                "id",                p.getId(),
                "fullName",          p.getFullName(),
                "phone",             p.getPhone(),
                "status",            p.getStatus(),
                "averageRating",     p.getAverageRating(),
                "totalAcresSprayed", p.getTotalAcresSprayed()
        );
    }
}
