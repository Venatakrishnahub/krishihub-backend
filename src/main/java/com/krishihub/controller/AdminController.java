package com.krishihub.controller;

import com.krishihub.dto.ApiResponse;
import com.krishihub.repository.BookingRepository;
import com.krishihub.repository.FarmerRepository;
import com.krishihub.repository.PilotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final FarmerRepository farmerRepo;
    private final PilotRepository  pilotRepo;
    private final BookingRepository bookingRepo;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of(
                "totalFarmers",    farmerRepo.count(),
                "totalPilots",     pilotRepo.count(),
                "activePilots",    pilotRepo.countByStatus("active"),
                "onlinePilots",    pilotRepo.countByIsOnlineTrue(),
                "totalBookings",   bookingRepo.count(),
                "pendingBookings", bookingRepo.countByStatus("pending"),
                "completedToday",  bookingRepo.countByStatusAndScheduledDate(
                        "completed", LocalDate.now())
        ));
    }

    @GetMapping("/analytics/revenue")
    public ResponseEntity<Map<String, Object>> revenue(
            @RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(Map.of(
                "period", period,
                "data", List.of(
                        Map.of("label","Oct","revenue",85000,"acres",210),
                        Map.of("label","Nov","revenue",110000,"acres",280),
                        Map.of("label","Dec","revenue",62000,"acres",155),
                        Map.of("label","Jan","revenue",145000,"acres",368),
                        Map.of("label","Feb","revenue",98000,"acres",248),
                        Map.of("label","Mar","revenue",130000,"acres",328)
                )
        ));
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, String>> getSettings() {
        return ResponseEntity.ok(Map.of(
                "platform_fee_per_acre", "5.00",
                "morning_shift_start",   "06:00",
                "morning_shift_end",     "12:00",
                "afternoon_shift_start", "12:00",
                "afternoon_shift_end",   "18:00",
                "razorpay_mode",         "test",
                "otp_validity_minutes",  "10"
        ));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse> updateSettings(
            @RequestBody Map<String, String> settings) {
        return ResponseEntity.ok(ApiResponse.success("Settings saved"));
    }
}
