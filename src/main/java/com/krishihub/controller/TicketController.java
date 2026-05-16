package com.krishihub.controller;

import com.krishihub.dto.ApiResponse;
import com.krishihub.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse> create(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(
                ApiResponse.success("Ticket raised. We'll respond within 24 hours."));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> mine(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(List.of(
                Map.of("id","TK001","subject","Payment issue",
                       "status","open","date","2024-03-15")
        ));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> all(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(List.of(
                Map.of("id","TK001","subject","Payment not received",
                       "from","Farmer","user","Ramu Reddy",
                       "priority","high","status","open","date","2024-03-15"),
                Map.of("id","TK002","subject","Pilot did not show",
                       "from","Farmer","user","Krishna Rao",
                       "priority","urgent","status","in_progress","date","2024-03-14")
        ));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<ApiResponse> reply(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> req) {
        return ResponseEntity.ok(ApiResponse.success("Reply sent"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> req) {
        return ResponseEntity.ok(ApiResponse.success("Status updated"));
    }
}
