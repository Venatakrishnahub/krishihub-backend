package com.krishihub.repository;

import com.krishihub.model.Pilot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PilotRepository extends JpaRepository<Pilot, Long> {

    Optional<Pilot> findByPhone(String phone);

    List<Pilot> findByStatusAndIsOnlineTrue(String status);

    Page<Pilot> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);

    long countByIsOnlineTrue();

    @Query("SELECT p FROM Pilot p WHERE p.status = 'active' AND p.isOnline = true AND " +
           "p.currentLatitude IS NOT NULL AND p.currentLongitude IS NOT NULL AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(p.currentLatitude)) * " +
           "cos(radians(p.currentLongitude) - radians(:lng)) + " +
           "sin(radians(:lat)) * sin(radians(p.currentLatitude)))) < p.serviceAreaRadiusKm")
    List<Pilot> findNearbyOnlinePilots(@Param("lat") double lat, @Param("lng") double lng);
}
