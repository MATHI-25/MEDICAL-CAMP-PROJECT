package com.mediq.service.impl;

import com.mediq.service.SmsGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
public class SmsGatewayServiceImpl implements SmsGatewayService {

    private static final Logger log = LoggerFactory.getLogger(SmsGatewayServiceImpl.class);

    @Value("${app.sms.enabled:true}")
    private boolean smsEnabled;

    @Value("${app.sms.provider:MOCK}")
    private String smsProvider;

    @Value("${app.sms.twilio.account-sid:AC_demo_twilio_account_sid}")
    private String twilioAccountSid;

    @Value("${app.sms.twilio.auth-token:demo_twilio_auth_token}")
    private String twilioAuthToken;

    @Value("${app.sms.twilio.from-number:+1234567890}")
    private String twilioFromNumber;

    @Value("${app.sms.fast2sms.api-key:demo_fast2sms_api_key}")
    private String fast2smsApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean sendPhysicalSms(String recipientPhone, String messageText) {
        if (!smsEnabled) {
            log.info("SMS Gateway is disabled in application properties. Skipping cellular dispatch.");
            return false;
        }

        String cleanPhone = sanitizePhoneNumber(recipientPhone);
        log.info("DISPATCHING PHYSICAL SMS via [{}] Gateway to [{}]", smsProvider, cleanPhone);

        try {
            if ("TWILIO".equalsIgnoreCase(smsProvider)) {
                return sendViaTwilio(cleanPhone, messageText);
            } else if ("FAST2SMS".equalsIgnoreCase(smsProvider)) {
                return sendViaFast2Sms(cleanPhone, messageText);
            } else {
                log.info("SMS GATEWAY (MOCK/SIMULATED) -> Destination: {}\nContent:\n{}", cleanPhone, messageText);
                return true;
            }
        } catch (Exception e) {
            log.error("Failed to transmit cellular SMS via Gateway: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean sendViaTwilio(String toPhone, String body) {
        try {
            String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", twilioAccountSid);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(twilioAccountSid, twilioAuthToken, StandardCharsets.UTF_8);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("To", toPhone);
            map.add("From", twilioFromNumber);
            map.add("Body", body);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(URI.create(url), request, String.class);

            boolean ok = response.getStatusCode().is2xxSuccessful();
            log.info("TWILIO SMS DISPATCH STATUS: {} | Response: {}", response.getStatusCode(), response.getBody());
            return ok;
        } catch (Exception e) {
            log.warn("Twilio HTTP Dispatch Error: {}", e.getMessage());
            return false;
        }
    }

    private boolean sendViaFast2Sms(String toPhone, String body) {
        try {
            String url = "https://www.fast2sms.com/dev/bulkV2";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authorization", fast2smsApiKey);

            String digitsOnly = toPhone.replaceAll("[^0-9]", "");
            if (digitsOnly.length() > 10) {
                digitsOnly = digitsOnly.substring(digitsOnly.length() - 10);
            }

            String jsonPayload = String.format(
                "{\"route\":\"q\",\"message\":\"%s\",\"language\":\"english\",\"numbers\":\"%s\"}",
                escapeJson(body), digitsOnly
            );

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            boolean ok = response.getStatusCode().is2xxSuccessful();
            log.info("FAST2SMS DISPATCH STATUS: {} | Response: {}", response.getStatusCode(), response.getBody());
            return ok;
        } catch (Exception e) {
            log.warn("Fast2SMS HTTP Dispatch Error: {}", e.getMessage());
            return false;
        }
    }

    private String sanitizePhoneNumber(String raw) {
        if (raw == null || raw.isBlank()) return "+15550199";
        String clean = raw.replaceAll("[^0-9+]", "");
        if (!clean.startsWith("+")) {
            clean = "+91" + clean; // Default to country code +91
        }
        return clean;
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
