package com.krishihub.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OtpService {

    @Value("${twilio.account-sid}") private String accountSid;
    @Value("${twilio.auth-token}")  private String authToken;
    @Value("${twilio.phone-number}") private String fromPhone;

    public void sendSms(String toPhone, String message) {
        try {
            Twilio.init(accountSid, authToken);
            Message.creator(
                new PhoneNumber("+91" + toPhone),
                new PhoneNumber(fromPhone),
                message
            ).create();
            log.info("SMS sent to +91{}", toPhone);
        } catch (Exception e) {
            log.error("SMS send failed to {}: {}", toPhone, e.getMessage());
        }
    }
}
