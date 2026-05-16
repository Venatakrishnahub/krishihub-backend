package com.krishihub.controller;

import com.krishihub.dto.*;
import com.krishihub.service.PilotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pilots")
@RequiredArgsConstructor
public class PilotController {

    private final PilotService pilotService;

    // ── ADMIN ─────────────────────────────────────────────────────

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> register(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody RegisterPilotRequest req) {
        pilotService.registerPilot(token, req);
        return ResponseEntity.ok(ApiResponse.success("Pilot registered. OTP sent."));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest req) {
        pilotService.updateStatus(id, req.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Status updated"));
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<PilotResponse>> listAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String district,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(pilotService.getAllPilots(status, district, page, size));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PilotDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(pilotService.getPilotDetail(id));
    }

    @GetMapping("/admin/{id}/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PilotAnalyticsResponse> analytics(
            @PathVariable Long id,
            @RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(pilotService.getPilotAnalytics(id, period));
    }

    // ── PILOT ─────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<PilotDashboardResponse> dashboard(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(pilotService.getDashboard(token));
    }

    @PutMapping("/online-status")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<ApiResponse> onlineStatus(
            @RequestHeader("Authorization") String token,
            @RequestBody OnlineStatusRequest req) {
        pilotService.updateOnlineStatus(token, req.isOnline(),
                req.getLatitude(), req.getLongitude());
        return ResponseEntity.ok(ApiResponse.success(
                req.isOnline() ? "You are online" : "You are offline"));
    }

    @PostMapping("/location")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<ApiResponse> location(
            @RequestHeader("Authorization") String token,
            @RequestBody LocationUpdateRequest req) {
        pilotService.updateLocation(token, req.getLatitude(), req.getLongitude());
        return ResponseEntity.ok(ApiResponse.success("Location updated"));
    }

    @GetMapping("/earnings")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<EarningsResponse> earnings(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(pilotService.getEarnings(token, period));
    }

    @GetMapping("/daily-report")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<List<DailyReportResponse>> dailyReport(
            @RequestHeader("Authorization") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(pilotService.getDailyReport(token, fromDate, toDate));
    }

    @PostMapping("/upload-document")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<ApiResponse> uploadDoc(
            @RequestHeader("Authorization") String token,
            @RequestParam String docType,
            @RequestParam MultipartFile file) {
        String url = pilotService.uploadDocument(token, docType, file);
        return ResponseEntity.ok(ApiResponse.success("Uploaded: " + url));
    }
}
