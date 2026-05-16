package com.krishihub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CouponService {

    public Map<String, Object> validate(String code, double acres, int serviceTypeId) {
        if ("KISAN50".equalsIgnoreCase(code)) {
            if (acres < 5) throw new RuntimeException("Minimum 5 acres required for KISAN50");
            return Map.of("valid", true, "code", "KISAN50",
                    "discountAmount", 50.0, "message", "Coupon applied! You save ₹50");
        }
        if ("FIRST10".equalsIgnoreCase(code)) {
            double base = basePrice(serviceTypeId) * acres;
            double disc = base * 0.10;
            return Map.of("valid", true, "code", "FIRST10",
                    "discountAmount", disc, "message", "10% discount applied!");
        }
        throw new RuntimeException("Invalid or expired coupon code");
    }

    public List<Map<String, Object>> getAll() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", 1, "code", "KISAN50", "type", "fixed",
                "value", 50.0, "minAcres", 5.0, "isActive", true));
        list.add(Map.of("id", 2, "code", "FIRST10", "type", "percentage",
                "value", 10.0, "minAcres", 1.0, "isActive", true));
        return list;
    }

    public void create(Map<String, Object> req) {
        log.info("Coupon created: {}", req.get("code"));
    }

    public void update(Long id, Map<String, Object> req) {
        log.info("Coupon updated id={}", id);
    }

    public void delete(Long id) {
        log.info("Coupon deleted id={}", id);
    }

    private double basePrice(int serviceTypeId) {
        return switch (serviceTypeId) {
            case 2 -> 350; case 3 -> 500; case 4 -> 380; case 5 -> 420;
            default -> 400;
        };
    }
}
