package com.krishihub.service;

import com.krishihub.dto.*;
import com.krishihub.model.*;
import com.krishihub.repository.*;
import com.krishihub.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepo;
    private final FarmerRepository farmerRepo;
    private final PilotRepository pilotRepo;
    private final PaymentRepository paymentRepo;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;

    private static final BigDecimal PLATFORM_FEE = new BigDecimal("5.00");

    // ── CREATE ────────────────────────────────────────────────────
    @Transactional
    public BookingResponse createBooking(String rawToken, CreateBookingRequest req) {
        Long farmerId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Farmer farmer = farmerRepo.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        BigDecimal acres      = BigDecimal.valueOf(req.getAreaAcres());
        BigDecimal price      = servicePrice(req.getServiceTypeId());
        BigDecimal svcTotal   = price.multiply(acres);
        BigDecimal fee        = PLATFORM_FEE.multiply(acres);
        BigDecimal bonus      = req.getPilotBonus() != null
                ? BigDecimal.valueOf(req.getPilotBonus()) : BigDecimal.ZERO;
        BigDecimal total      = svcTotal.add(fee).add(bonus);

        Booking booking = Booking.builder()
                .farmerId(farmerId)
                .serviceTypeId(req.getServiceTypeId())
                .fieldVillage(req.getFieldVillage())
                .fieldMandal(req.getFieldMandal())
                .fieldDistrict(req.getFieldDistrict())
                .fieldLatitude(req.getFieldLatitude())
                .fieldLongitude(req.getFieldLongitude())
                .areaAcres(req.getAreaAcres())
                .cropType(req.getCropType())
                .surveyNumber(req.getSurveyNumber())
                .scheduledDate(req.getScheduledDate())
                .shift(req.getShift())
                .requiresMixer(req.isRequiresMixer())
                .mixerName(req.getMixerName())
                .mixerPhone(req.getMixerPhone())
                .chemicalName(req.getChemicalName())
                .chemicalQuantityLiters(req.getChemicalQuantityLiters())
                .waterQuantityLiters(req.getWaterQuantityLiters())
                .specialInstructions(req.getSpecialInstructions())
                .servicePricePerAcre(price)
                .totalServicePrice(svcTotal)
                .platformFee(fee)
                .pilotBonus(bonus)
                .couponCode(req.getCouponCode())
                .totalAmount(total)
                .bookingNumber(generateBookingNumber())
                .build();

        booking = bookingRepo.save(booking);

        // Notify nearby online pilots
        if (req.getFieldLatitude() != null && req.getFieldLongitude() != null) {
            List<Pilot> nearby = pilotRepo.findNearbyOnlinePilots(
                    req.getFieldLatitude(), req.getFieldLongitude());
            final Booking saved = booking;
            nearby.forEach(p -> notificationService.notifyNewBooking(p, saved));
            log.info("Notified {} pilots for booking {}", nearby.size(), booking.getId());
        }



        return toResponse(booking, farmer, null);
    }

    private String generateBookingNumber() {

        long random = (long)(Math.random() * 99_999_999L) + 1;

        return String.format(
                "KH%d%08d",
                java.time.LocalDate.now().getYear(),
                random
        );
    }

    // ── FARMER: list ──────────────────────────────────────────────
    public PagedResponse<BookingResponse> getFarmerBookings(
            String rawToken, String status, int page, int size) {
        Long farmerId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Pageable pr = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Booking> result = (status == null || status.equals("all"))
                ? bookingRepo.findByFarmerIdOrderByCreatedAtDesc(farmerId, pr)
                : bookingRepo.findByFarmerIdAndStatusOrderByCreatedAtDesc(farmerId, status, pr);

        List<BookingResponse> content = result.getContent().stream()
                .map(b -> toResponse(b, b.getFarmer(), b.getPilot()))
                .collect(Collectors.toList());

        return PagedResponse.<BookingResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    // ── FARMER: cancel ────────────────────────────────────────────
    @Transactional
    public void cancelByFarmer(String rawToken, Long bookingId, String reason) {
        Long farmerId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Booking b = findBooking(bookingId);
        if (!b.getFarmerId().equals(farmerId)) throw new RuntimeException("Not authorized");
        if (List.of("completed","in_progress","cancelled").contains(b.getStatus()))
            throw new RuntimeException("Cannot cancel booking in status: " + b.getStatus());
        b.setStatus("cancelled");
        b.setCancelledBy("farmer");
        b.setCancellationReason(reason);
        b.setCancelledAt(LocalDateTime.now());
        bookingRepo.save(b);
    }

    // ── PILOT: my jobs ───────────────────────────────────────────
    public PagedResponse<BookingResponse> getPilotMyJobs(
            String rawToken,
            int page,
            int size) {

        Long pilotId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));

        Pageable pr = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Booking> bookings =
                bookingRepo.findByPilotIdOrderByCreatedAtDesc(
                        pilotId,
                        pr
                );

        List<BookingResponse> content = bookings.getContent()
                .stream()
                .map(b -> toResponse(b, null, b.getPilot()))
                .collect(Collectors.toList());

        return PagedResponse.<BookingResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(bookings.getTotalElements())
                .totalPages(bookings.getTotalPages())
                .last(bookings.isLast())
                .build();
    }

    // ── PILOT: available jobs ─────────────────────────────────────
    public List<BookingResponse> getAvailableBookingsForPilot(String rawToken) {
        Long pilotId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Pilot pilot = pilotRepo.findById(pilotId)
                .orElseThrow(() -> new RuntimeException("Pilot not found"));
        return bookingRepo.findPendingBookingsByDistrict(pilot.getDistrict()).stream()
                .map(b -> toResponse(b, null, null))
                .collect(Collectors.toList());
    }

    // ── PILOT: accept ─────────────────────────────────────────────
    @Transactional
    public void pilotAcceptBooking(String rawToken, Long bookingId) {
        Long pilotId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Booking b = findBooking(bookingId);
        if (!"pending".equals(b.getStatus()))
            throw new RuntimeException("Booking no longer available");
        Pilot pilot = pilotRepo.findById(pilotId).orElseThrow();
        b.setPilotId(pilotId);
        b.setStatus("accepted");
        BigDecimal earning = b.getTotalServicePrice().add(b.getPilotBonus()).subtract(b.getPlatformFee());
        b.setPilotEarning(earning);
        bookingRepo.save(b);
        Farmer farmer = farmerRepo.findById(b.getFarmerId()).orElseThrow();
        notificationService.notifyBookingAccepted(farmer, b, pilot);
    }

    // ── PILOT: start → OTP ────────────────────────────────────────
    @Transactional
    public StartBookingResponse pilotStartBooking(String rawToken, Long bookingId) {
        Long pilotId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Booking b = findBooking(bookingId);
        if (!pilotId.equals(b.getPilotId()))
            throw new RuntimeException("Not your booking");
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        b.setStatus("in_progress");
        b.setOtpForCompletion(otp);
        b.setActualStartTime(LocalDateTime.now());
        bookingRepo.save(b);
        Farmer farmer = farmerRepo.findById(b.getFarmerId()).orElseThrow();
        notificationService.notifyServiceStarted(farmer, b);
        return StartBookingResponse.builder()
                .otp(otp).message("Service started. OTP sent to farmer.").build();
    }

    // ── FARMER: verify OTP ────────────────────────────────────────
    @Transactional
    public void verifyCompletionOtp(String rawToken, Long bookingId, String otp) {
        Booking b = findBooking(bookingId);
        if (!"in_progress".equals(b.getStatus()))
            throw new RuntimeException("Booking is not in progress");
        if (!otp.equals(b.getOtpForCompletion()))
            throw new RuntimeException("Invalid OTP");
        b.setOtpVerified(true);
        b.setOtpVerifiedAt(LocalDateTime.now());
        b.setStatus("completed");
        b.setActualEndTime(LocalDateTime.now());
        b.setActualAcresSprayed(b.getAreaAcres());
        bookingRepo.save(b);

        // Update pilot stats
        if (b.getPilotId() != null) {
            pilotRepo.findById(b.getPilotId()).ifPresent(p -> {
                p.setTotalAcresSprayed(
                        (p.getTotalAcresSprayed() != null ? p.getTotalAcresSprayed() : 0.0)
                                + b.getAreaAcres());
                p.setTotalBookingsCompleted(
                        (p.getTotalBookingsCompleted() != null ? p.getTotalBookingsCompleted() : 0) + 1);
                pilotRepo.save(p);
            });
        }
        Farmer farmer = farmerRepo.findById(b.getFarmerId()).orElseThrow();
        notificationService.notifyServiceCompleted(farmer, b);
    }

    // ── PILOT: reschedule ─────────────────────────────────────────
    @Transactional
    public void pilotRescheduleBooking(String rawToken, Long bookingId, RescheduleRequest req) {
        Long pilotId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Booking b = findBooking(bookingId);
        if (!pilotId.equals(b.getPilotId()))
            throw new RuntimeException("Not your booking");
        b.setStatus("rescheduled");
        b.setRescheduledDate(req.getNewDate());
        b.setRescheduledShift(req.getNewShift());
        b.setRescheduleReason(req.getReason());
        b.setRescheduledBy("pilot");
        bookingRepo.save(b);
        Farmer farmer = farmerRepo.findById(b.getFarmerId()).orElseThrow();
        notificationService.notifyBookingRescheduled(farmer, b, req.getReason());
    }

    // ── PILOT: submit payment (cash/cheque) ───────────────────────
    @Transactional
    public void pilotSubmitPayment(String rawToken, Long bookingId, PilotPaymentRequest req) {
        Long pilotId = jwtUtil.extractUserId(rawToken.replace("Bearer ", ""));
        Booking b = findBooking(bookingId);
        Payment p = Payment.builder()
                .bookingId(bookingId)
                .farmerId(b.getFarmerId())
                .pilotId(pilotId)
                .paymentMode(Payment.PaymentMode.valueOf(req.getPaymentMode().toUpperCase()))
                .paymentDirection(Payment.PaymentDirection.FARMER_TO_PLATFORM)
                .amount(b.getTotalAmount().doubleValue())
                .cashSubmittedByPilot(true)
                .cashSubmissionPhoto(req.getPhotoUrl())
                .chequeNumber(req.getChequeNumber())
                .chequeDate(req.getChequeDate())
                .bankName(req.getBankName())
                .build();
        paymentRepo.save(p);
    }

    // ── ADMIN ─────────────────────────────────────────────────────
    public PagedResponse<BookingResponse> getAllBookings(
            String status, String district, String date, int page, int size) {
        Pageable pr = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Booking> result = bookingRepo.findAll(pr);
        List<BookingResponse> content = result.getContent().stream()
                .map(b -> toResponse(b, null, null)).collect(Collectors.toList());
        return PagedResponse.<BookingResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages()).last(result.isLast()).build();
    }

    @Transactional
    public void adminAssignPilot(Long bookingId, Long pilotId) {
        Booking b = findBooking(bookingId);
        Pilot pilot = pilotRepo.findById(pilotId).orElseThrow();
        b.setPilotId(pilotId);
        b.setStatus("accepted");
        bookingRepo.save(b);
        Farmer farmer = farmerRepo.findById(b.getFarmerId()).orElseThrow();
        notificationService.notifyBookingAccepted(farmer, b, pilot);
    }

    public BookingResponse getBookingById(String rawToken, Long bookingId) {
        Booking b = findBooking(bookingId);
        return toResponse(b, b.getFarmer(), b.getPilot());
    }

    public BookingEstimateResponse calculateEstimate(BookingEstimateRequest req) {
        BigDecimal acres  = BigDecimal.valueOf(req.getAreaAcres());
        BigDecimal price  = servicePrice(req.getServiceTypeId());
        BigDecimal svc    = price.multiply(acres);
        BigDecimal fee    = PLATFORM_FEE.multiply(acres);
        BigDecimal bonus  = req.getPilotBonus() != null
                ? BigDecimal.valueOf(req.getPilotBonus()) : BigDecimal.ZERO;
        return BookingEstimateResponse.builder()
                .servicePrice(svc.doubleValue()).platformFee(fee.doubleValue())
                .pilotBonus(bonus.doubleValue()).discountAmount(0.0)
                .totalAmount(svc.add(fee).add(bonus).doubleValue()).build();
    }

    public void submitRating(String rawToken, Long bookingId, RatingRequest req) {
        log.info("Rating {} submitted for booking {}", req.getRating(), bookingId);
    }

    // ── private helpers ───────────────────────────────────────────

    private Booking findBooking(Long id) {
        return bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
    }

    private BigDecimal servicePrice(Long serviceTypeId) {
        if (serviceTypeId == null) return new BigDecimal("400");
        return switch (serviceTypeId.intValue()) {
            case 1 -> new BigDecimal("400");
            case 2 -> new BigDecimal("350");
            case 3 -> new BigDecimal("500");
            case 4 -> new BigDecimal("380");
            case 5 -> new BigDecimal("420");
            default -> new BigDecimal("400");
        };
    }

    private BookingResponse toResponse(Booking b, Farmer farmer, Pilot pilot) {
        return BookingResponse.builder()
                .id(b.getId())
                .bookingNumber(b.getBookingNumber())
                .status(b.getStatus())
                .areaAcres(b.getAreaAcres())
                .fieldVillage(b.getFieldVillage())
                .fieldDistrict(b.getFieldDistrict())
                .scheduledDate(b.getScheduledDate() != null ? b.getScheduledDate().toString() : null)
                .shift(b.getShift())
                .totalAmount(b.getTotalAmount() != null ? b.getTotalAmount().doubleValue() : 0.0)
                .pilotEarning(b.getPilotEarning() != null ? b.getPilotEarning().doubleValue() : null)
                .pilotName(pilot != null ? pilot.getFullName() : null)
                .pilotPhone(pilot != null ? pilot.getPhone() : null)
                .otpForCompletion(b.getOtpForCompletion())
                .otpVerified(b.isOtpVerified())
                .createdAt(b.getCreatedAt() != null ? b.getCreatedAt().toString() : null)
                .build();
    }
}
