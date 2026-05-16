package com.krishihub.controller;

import com.krishihub.dto.*;
import com.krishihub.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ── FARMER ────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<BookingResponse> create(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateBookingRequest req) {
        return ResponseEntity.ok(bookingService.createBooking(token, req));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<PagedResponse<BookingResponse>> myBookings(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingService.getFarmerBookings(token, status, page, size));
    }

    @PostMapping("/estimate")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<BookingEstimateResponse> estimate(
            @RequestBody BookingEstimateRequest req) {
        return ResponseEntity.ok(bookingService.calculateEstimate(req));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse> cancel(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody CancelRequest req) {
        bookingService.cancelByFarmer(token, id, req.getReason());
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled"));
    }

    @PostMapping("/{id}/verify-completion")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse> verifyCompletion(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody OtpVerifyRequest req) {
        bookingService.verifyCompletionOtp(token, id, req.getOtp());
        return ResponseEntity.ok(ApiResponse.success("Service verified!"));
    }

    @PostMapping("/{id}/rate")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse> rate(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest req) {
        bookingService.submitRating(token, id, req);
        return ResponseEntity.ok(ApiResponse.success("Rating submitted"));
    }

    // ── PILOT ─────────────────────────────────────────────────────

    @GetMapping("/pilot/available")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<List<BookingResponse>> available(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(bookingService.getAvailableBookingsForPilot(token));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<ApiResponse> accept(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        bookingService.pilotAcceptBooking(token, id);
        return ResponseEntity.ok(ApiResponse.success("Booking accepted!"));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<StartBookingResponse> start(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.pilotStartBooking(token, id));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<ApiResponse> reschedule(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequest req) {
        bookingService.pilotRescheduleBooking(token, id, req);
        return ResponseEntity.ok(ApiResponse.success("Rescheduled. Farmer notified."));
    }

    @PostMapping("/{id}/submit-payment")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<ApiResponse> submitPayment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody PilotPaymentRequest req) {
        bookingService.pilotSubmitPayment(token, id, req);
        return ResponseEntity.ok(ApiResponse.success("Payment submitted"));
    }

    @GetMapping("/pilot/my-jobs")
    @PreAuthorize("hasRole('PILOT')")
    public ResponseEntity<PagedResponse<BookingResponse>> pilotMyJobs(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
                bookingService.getPilotMyJobs(
                        token,
                        page,
                        size
                )
        );
    }

    // ── SHARED ────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(token, id));
    }

    // ── ADMIN ─────────────────────────────────────────────────────

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<BookingResponse>> all(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bookingService.getAllBookings(status, district, date, page, size));
    }

    @PutMapping("/{id}/assign-pilot")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> assignPilot(
            @PathVariable Long id,
            @RequestBody AssignPilotRequest req) {
        bookingService.adminAssignPilot(id, req.getPilotId());
        return ResponseEntity.ok(ApiResponse.success("Pilot assigned"));
    }
}
