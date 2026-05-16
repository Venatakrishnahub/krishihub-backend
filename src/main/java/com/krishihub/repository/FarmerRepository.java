package com.krishihub.repository;

import com.krishihub.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    Optional<Farmer> findByPhone(String phone);
    boolean existsByPhone(String phone);
    boolean existsByAadhaarNumber(String aadhaarNumber);
}
