package com.krishihub.controller;

import com.krishihub.dto.ApiResponse;
import com.krishihub.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<Map<String, Object>> validate(
            @RequestBody Map<String, Object> req) {
        String code = (String) req.get("code");
        double acres = ((Number) req.get("acres")).doubleValue();
        int serviceTypeId = ((Number) req.get("serviceTypeId")).intValue();
        return ResponseEntity.ok(couponService.validate(code, acres, serviceTypeId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(couponService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> create(
            @RequestBody Map<String, Object> req) {
        couponService.create(req);
        return ResponseEntity.ok(ApiResponse.success("Coupon created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req) {
        couponService.update(id, req);
        return ResponseEntity.ok(ApiResponse.success("Coupon updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted"));
    }
}
