package com.krishihub.controller;

import com.krishihub.dto.*;
import com.krishihub.model.Farmer;
import com.krishihub.repository.FarmerRepository;
import com.krishihub.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/farmers")
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerRepository farmerRepo;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<Farmer> me(
            @RequestHeader("Authorization") String token) {
        Long id = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Farmer f = farmerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
        return ResponseEntity.ok(f);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse> update(
            @RequestHeader("Authorization") String token,
            @RequestBody FarmerProfileRequest req) {
        Long id = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Farmer f = farmerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
        if (req.getFullName() != null)          f.setFullName(req.getFullName());
        if (req.getVillage() != null)           f.setVillage(req.getVillage());
        if (req.getMandal() != null)            f.setMandal(req.getMandal());
        if (req.getDistrict() != null)          f.setDistrict(req.getDistrict());
        if (req.getPreferredLanguage() != null) f.setPreferredLanguage(req.getPreferredLanguage());
        farmerRepo.save(f);
        return ResponseEntity.ok(ApiResponse.success("Profile updated"));
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Farmer>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(farmerRepo.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Farmer> getById(@PathVariable Long id) {
        return ResponseEntity.ok(farmerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found")));
    }
}
