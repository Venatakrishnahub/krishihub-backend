package com.krishihub.service;

import com.krishihub.dto.*;
import com.krishihub.model.Pilot;
import com.krishihub.repository.PilotRepository;
import com.krishihub.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PilotService {

    private final PilotRepository pilotRepo;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    // ── ADMIN: register ───────────────────────────────────────────
    @Transactional
    public void registerPilot(String rawToken, RegisterPilotRequest req) {
        if (pilotRepo.findByPhone(req.getPhone()).isPresent()) {
            throw new RuntimeException("Pilot with this phone already exists");
        }
        Long adminId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));

        Pilot pilot = Pilot.builder()
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .aadhaarNumber(req.getAadhaarNumber())
                .village(req.getVillage())
                .mandal(req.getMandal())
                .district(req.getDistrict())
                .dronePilotLicense(req.getDronePilotLicense())
                .licenseExpiryDate(req.getLicenseExpiryDate())
                .dgcaUinNumber(req.getDgcaUinNumber())
                .bankName(req.getBankName())
                .accountNumber(req.getAccountNumber())
                .ifscCode(req.getIfscCode())
                .upiId(req.getUpiId())
                .passwordHash(passwordEncoder.encode(tempPassword()))
                .registeredByAdmin(adminId)
                .registrationNotes(req.getRegistrationNotes())
                .build();

        pilotRepo.save(pilot);
        otpService.sendSms(req.getPhone(),
                "Welcome to KrishiHub! You are registered as a drone pilot. Login with OTP. - KrishiHub");
        log.info("Pilot {} registered by admin {}", req.getPhone(), adminId);
    }

    // ── ADMIN: status ─────────────────────────────────────────────
    @Transactional
    public void updateStatus(Long pilotId, String status) {
        Pilot p = pilotRepo.findById(pilotId)
                .orElseThrow(() -> new RuntimeException("Pilot not found"));
        p.setStatus(status);
        pilotRepo.save(p);
    }

    // ── PILOT: dashboard ──────────────────────────────────────────
//    public PilotDashboardResponse getDashboard(String rawToken) {
//        Long id = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
//        Pilot p = pilotRepo.findById(id).orElseThrow();
//        return PilotDashboardResponse.builder()
//                .todayAcres(10.5).todayEarnings(4200.0).todayJobs(3)
//                .monthAcres(p.getTotalAcresSprayed())
//                .monthEarnings(45000.0)
//                .monthJobs(p.getTotalBookingsCompleted())
//                .totalAcres(p.getTotalAcresSprayed())
//                .totalEarnings(p.getTotalEarnings() != null
//                        ? p.getTotalEarnings().doubleValue() : 0.0)
//                .pendingJobs(2)
//                .build();
//    }

    // ── PILOT: dashboard ──────────────────────────────────────────
    public PilotDashboardResponse getDashboard(String rawToken) {
        Long id = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Pilot p = pilotRepo.findById(id).orElseThrow();

        // Real data from DB
        return PilotDashboardResponse.builder()
                .todayAcres(p.getTotalAcresSprayed() != null ? p.getTotalAcresSprayed() : 0.0)
                .todayEarnings(p.getTotalEarnings() != null ? p.getTotalEarnings().doubleValue() : 0.0)
                .todayJobs(p.getTotalBookingsCompleted() != null ? p.getTotalBookingsCompleted() : 0)
                .monthAcres(p.getTotalAcresSprayed() != null ? p.getTotalAcresSprayed() : 0.0)
                .monthEarnings(p.getTotalEarnings() != null ? p.getTotalEarnings().doubleValue() : 0.0)
                .monthJobs(p.getTotalBookingsCompleted() != null ? p.getTotalBookingsCompleted() : 0)
                .totalAcres(p.getTotalAcresSprayed() != null ? p.getTotalAcresSprayed() : 0.0)
                .totalEarnings(p.getTotalEarnings() != null ? p.getTotalEarnings().doubleValue() : 0.0)
                .pendingJobs(0)
                .build();
    }

    // ── PILOT: earnings ───────────────────────────────────────────
    public EarningsResponse getEarnings(String rawToken, String period) {
        Long id = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Pilot p = pilotRepo.findById(id).orElseThrow();
        return EarningsResponse.builder()
                .totalEarnings(p.getTotalEarnings() != null
                        ? p.getTotalEarnings().doubleValue() : 0.0)
                .totalAcres(p.getTotalAcresSprayed() != null
                        ? p.getTotalAcresSprayed() : 0.0)
                .totalJobs(p.getTotalBookingsCompleted() != null
                        ? p.getTotalBookingsCompleted() : 0)
                .build();
    }

    // ── PILOT: online status ──────────────────────────────────────
    @Transactional
    public void updateOnlineStatus(String rawToken, boolean online,
                                   Double lat, Double lng) {
        Long id = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        pilotRepo.findById(id).ifPresent(p -> {
            p.setOnline(online);
            if (lat != null) p.setCurrentLatitude(lat);
            if (lng != null) p.setCurrentLongitude(lng);
            pilotRepo.save(p);
        });
    }

    // ── PILOT: location update ────────────────────────────────────
    @Transactional
    public void updateLocation(String rawToken, Double lat, Double lng) {
        Long id = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        pilotRepo.findById(id).ifPresent(p -> {
            p.setCurrentLatitude(lat);
            p.setCurrentLongitude(lng);
            pilotRepo.save(p);
        });
    }

    // ── PILOT: earnings ───────────────────────────────────────────
//    public EarningsResponse getEarnings(String rawToken, String period) {
//        Long id = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
//        Pilot p = pilotRepo.findById(id).orElseThrow();
//        return EarningsResponse.builder()
//                .totalEarnings(p.getTotalEarnings() != null
//                        ? p.getTotalEarnings().doubleValue() : 0.0)
//                .totalAcres(p.getTotalAcresSprayed())
//                .totalJobs(p.getTotalBookingsCompleted())
//                .build();
//    }

    // ── PILOT: daily report ───────────────────────────────────────
    public List<DailyReportResponse> getDailyReport(String rawToken,
                                                     LocalDate from, LocalDate to) {
        // In production: query pilot_daily_reports table
        return List.of(
                DailyReportResponse.builder().date(from.toString())
                        .acres(8.5).earnings(3400.0).jobs(2)
                        .districts("East Godavari").build(),
                DailyReportResponse.builder().date(to.toString())
                        .acres(12.0).earnings(4800.0).jobs(3)
                        .districts("West Godavari").build()
        );
    }

    // ── PILOT: upload document ────────────────────────────────────
    public String uploadDocument(String rawToken, String docType, MultipartFile file) {
        // In production: upload to AWS S3
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        log.info("Document uploaded: {} for type: {}", filename, docType);
        return "/uploads/" + filename;
    }

    // ── ADMIN: list all pilots ────────────────────────────────────
    public PagedResponse<PilotResponse> getAllPilots(String status, String district,
                                                      int page, int size) {
        Pageable pr = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Pilot> pilots = (status != null && !status.isBlank())
                ? pilotRepo.findByStatus(status, pr)
                : pilotRepo.findAll(pr);
        List<PilotResponse> content = pilots.getContent().stream()
                .map(this::toResponse).collect(Collectors.toList());
        return PagedResponse.<PilotResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(pilots.getTotalElements())
                .totalPages(pilots.getTotalPages())
                .last(pilots.isLast()).build();
    }

    // ── ADMIN: pilot detail ───────────────────────────────────────
    public PilotDetailResponse getPilotDetail(Long id) {
        Pilot p = pilotRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Pilot not found"));
        return PilotDetailResponse.builder()
                .id(p.getId()).fullName(p.getFullName()).phone(p.getPhone())
                .email(p.getEmail()).aadhaarNumber(p.getAadhaarNumber())
                .aadhaarVerified(p.isAadhaarVerified())
                .dronePilotLicense(p.getDronePilotLicense())
                .licenseExpiryDate(p.getLicenseExpiryDate())
                .district(p.getDistrict()).status(p.getStatus())
                .averageRating(p.getAverageRating()).totalRatings(p.getTotalRatings())
                .totalAcresSprayed(p.getTotalAcresSprayed())
                .totalBookingsCompleted(p.getTotalBookingsCompleted())
                .totalEarnings(p.getTotalEarnings() != null
                        ? p.getTotalEarnings().doubleValue() : 0.0)
                .isOnline(p.isOnline()).build();
    }

    // ── ADMIN: analytics ──────────────────────────────────────────
    public PilotAnalyticsResponse getPilotAnalytics(Long id, String period) {
        return PilotAnalyticsResponse.builder().avgEarningPerAcre(395.0).build();
    }

    private PilotResponse toResponse(Pilot p) {
        return PilotResponse.builder()
                .id(p.getId()).fullName(p.getFullName()).phone(p.getPhone())
                .district(p.getDistrict()).status(p.getStatus())
                .averageRating(p.getAverageRating())
                .totalAcresSprayed(p.getTotalAcresSprayed())
                .isOnline(p.isOnline()).build();
    }

    private String tempPassword() {
        return String.format("%08d", new Random().nextInt(100_000_000));
    }
}
