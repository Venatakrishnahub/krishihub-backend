package com.krishihub.service;

import com.google.firebase.messaging.*;
import com.krishihub.model.Booking;
import com.krishihub.model.Farmer;
import com.krishihub.model.Pilot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    // null when firebase-service-account.json is not configured
    @Autowired(required = false)
    private FirebaseMessaging firebaseMessaging;

    private void send(String fcmToken, String title, String body,
                      Map<String, String> data) {
        if (firebaseMessaging == null || fcmToken == null || fcmToken.isBlank()) return;
        try {
            Message msg = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title).setBody(body).build())
                    .putAllData(data != null ? data : new HashMap<>())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH).build())
                    .build();
            String response = firebaseMessaging.send(msg);
            log.debug("FCM sent: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM error: {}", e.getMessage());
        }
    }

    public void notifyNewBooking(Pilot pilot, Booking booking) {
        Map<String, String> data = bookingData(booking);
        data.put("type", "NEW_BOOKING");
        boolean te = "te".equals(pilot.getPreferredLanguage());
        send(pilot.getFcmToken(),
                te ? "కొత్త బుకింగ్ అభ్యర్థన!" : "New Booking Request!",
                String.format(te ? "%.1f ఎకరాలకు %s లో బుకింగ్"
                                : "New booking: %.1f acres in %s",
                        booking.getAreaAcres(), booking.getFieldVillage()), data);
    }

    public void notifyBookingAccepted(Farmer farmer, Booking booking, Pilot pilot) {
        Map<String, String> data = bookingData(booking);
        data.put("type", "BOOKING_ACCEPTED");
        data.put("pilotName", pilot.getFullName());
        data.put("pilotPhone", pilot.getPhone());
        boolean te = "te".equals(farmer.getPreferredLanguage());
        send(farmer.getFcmToken(),
                te ? "బుకింగ్ అంగీకరించబడింది!" : "Booking Accepted!",
                String.format(te ? "పైలట్ %s మీ బుకింగ్ అంగీకరించారు"
                                : "Pilot %s accepted your booking",
                        pilot.getFullName()), data);
    }

    public void notifyBookingRescheduled(Farmer farmer, Booking booking, String reason) {
        Map<String, String> data = bookingData(booking);
        data.put("type", "BOOKING_RESCHEDULED");
        boolean te = "te".equals(farmer.getPreferredLanguage());
        String newDate = booking.getRescheduledDate() != null
                ? booking.getRescheduledDate().toString() : "";
        send(farmer.getFcmToken(),
                te ? "బుకింగ్ తేదీ మారింది" : "Booking Rescheduled",
                String.format(te ? "మీ బుకింగ్ %s కి మార్చబడింది"
                        : "Your booking rescheduled to %s", newDate), data);
    }

    public void notifyServiceStarted(Farmer farmer, Booking booking) {
        Map<String, String> data = bookingData(booking);
        data.put("type", "SERVICE_STARTED");
        data.put("otp", booking.getOtpForCompletion());
        boolean te = "te".equals(farmer.getPreferredLanguage());
        send(farmer.getFcmToken(),
                te ? "పిచికారీ ప్రారంభమైంది!" : "Spraying Started!",
                String.format(te ? "పూర్తి OTP: %s" : "Completion OTP: %s",
                        booking.getOtpForCompletion()), data);
    }

    public void notifyServiceCompleted(Farmer farmer, Booking booking) {
        Map<String, String> data = bookingData(booking);
        data.put("type", "SERVICE_COMPLETED");
        boolean te = "te".equals(farmer.getPreferredLanguage());
        send(farmer.getFcmToken(),
                te ? "సేవ పూర్తయింది!" : "Service Completed!",
                String.format(te ? "%.1f ఎకరాలు పూర్తి చేయబడ్డాయి"
                                : "%.1f acres sprayed successfully",
                        booking.getActualAcresSprayed() != null
                                ? booking.getActualAcresSprayed()
                                : booking.getAreaAcres()), data);
    }

    public void notifyPaymentSuccess(Farmer farmer, Booking booking, double amount) {
        Map<String, String> data = bookingData(booking);
        data.put("type", "PAYMENT_SUCCESS");
        boolean te = "te".equals(farmer.getPreferredLanguage());
        send(farmer.getFcmToken(),
                te ? "చెల్లింపు విజయవంతమైంది" : "Payment Successful",
                String.format(te ? "₹%.0f చెల్లింపు నిర్ధారించబడింది"
                        : "₹%.0f payment confirmed", amount), data);
    }

    private Map<String, String> bookingData(Booking booking) {
        Map<String, String> d = new HashMap<>();
        d.put("bookingId", String.valueOf(booking.getId()));
        d.put("bookingNumber", booking.getBookingNumber() != null
                ? booking.getBookingNumber() : "");
        return d;
    }
}
