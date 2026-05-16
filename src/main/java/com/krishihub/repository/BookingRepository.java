package com.krishihub.repository;

import com.krishihub.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByFarmerIdOrderByCreatedAtDesc(Long farmerId, Pageable pageable);

    Page<Booking> findByFarmerIdAndStatusOrderByCreatedAtDesc(
            Long farmerId, String status, Pageable pageable);

    Page<Booking> findByPilotIdOrderByCreatedAtDesc(Long pilotId, Pageable pageable);

    long countByStatus(String status);

    long countByStatusAndScheduledDate(String status, LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.status = 'pending' AND b.pilotId IS NULL " +
           "AND (:district IS NULL OR b.fieldDistrict = :district) " +
           "ORDER BY b.createdAt ASC")
    List<Booking> findPendingBookingsByDistrict(@Param("district") String district);
}
