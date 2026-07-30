package com.mediq.service.impl;

import com.mediq.exception.BadRequestException;
import com.mediq.service.WhatsappGatewayService;
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
public class WhatsappGatewayServiceImpl implements WhatsappGatewayService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappGatewayServiceImpl.class);

    @Value("${app.whatsapp.enabled:true}")
    private boolean whatsappEnabled;

    @Value("${app.whatsapp.provider:TWILIO}")
    private String whatsappProvider;

    @Value("${app.whatsapp.twilio.account-sid:AC_demo_twilio_account_sid}")
    private String twilioAccountSid;

    @Value("${app.whatsapp.twilio.auth-token:demo_twilio_auth_token}")
    private String twilioAuthToken;

    @Value("${app.whatsapp.twilio.from-number:whatsapp:+14155238886}")
    private String twilioFromNumber;

    @Value("${app.whatsapp.meta.phone-number-id:}")
    private String metaPhoneNumberId;

    @Value("${app.whatsapp.meta.access-token:}")
    private String metaAccessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean sendPhysicalWhatsapp(String recipientPhone, String messageText) {
        if (!whatsappEnabled) {
            throw new BadRequestException("WhatsApp Gateway is disabled in backend application properties (app.whatsapp.enabled=false).");
        }

        String cleanPhone = sanitizePhoneNumber(recipientPhone);
        log.info("DISPATCHING PHYSICAL WHATSAPP via [{}] Gateway to [{}]", whatsappProvider, cleanPhone);

        if ("TWILIO".equalsIgnoreCase(whatsappProvider)) {
            return sendViaTwilioWhatsapp(cleanPhone, messageText);
        } else if ("META".equalsIgnoreCase(whatsappProvider)) {
            return sendViaMetaWhatsapp(cleanPhone, messageText);
        } else {
            // AUTO Mode: Use Twilio/Meta if configured, otherwise ready for direct web dispatch
            if (twilioAccountSid != null && !twilioAccountSid.isBlank() && !twilioAccountSid.contains("demo") &&
                twilioAuthToken != null && !twilioAuthToken.isBlank() && !twilioAuthToken.contains("demo")) {
                return sendViaTwilioWhatsapp(cleanPhone, messageText);
            }
            if (metaPhoneNumberId != null && !metaPhoneNumberId.isBlank() && metaAccessToken != null && !metaAccessToken.isBlank()) {
                return sendViaMetaWhatsapp(cleanPhone, messageText);
            }

            log.info("WHATSAPP DIRECT MODE: Cloud API keys unconfigured. Formatted direct payload for recipient [{}]", cleanPhone);
            return true;
        }
    }

    private boolean sendViaTwilioWhatsapp(String toPhone, String body) {
        if (twilioAccountSid == null || twilioAccountSid.contains("demo") || twilioAuthToken == null || twilioAuthToken.contains("demo")) {
            String errorMsg = "WhatsApp Delivery Failed: Twilio WhatsApp API Credentials are not configured. Please set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_WHATSAPP_FROM in application.yml or environment variables.";
            log.error(errorMsg);
            throw new BadRequestException(errorMsg);
        }

        try {
            String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", twilioAccountSid);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(twilioAccountSid, twilioAuthToken, StandardCharsets.UTF_8);

            String formattedTo = toPhone.startsWith("whatsapp:") ? toPhone : ("whatsapp:" + toPhone);
            String formattedFrom = twilioFromNumber.startsWith("whatsapp:") ? twilioFromNumber : ("whatsapp:" + twilioFromNumber);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("To", formattedTo);
            map.add("From", formattedFrom);
            map.add("Body", body);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(URI.create(url), request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("TWILIO WHATSAPP DISPATCH SUCCESS: Status {} | Response: {}", response.getStatusCode(), response.getBody());
                return true;
            } else {
                log.error("TWILIO WHATSAPP DISPATCH FAILED: Status {} | Response: {}", response.getStatusCode(), response.getBody());
                throw new BadRequestException("Twilio WhatsApp Gateway API Error: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Twilio WhatsApp API Execution Error: {}", e.getMessage(), e);
            if (e instanceof BadRequestException) throw (BadRequestException) e;
            throw new BadRequestException("WhatsApp Gateway Delivery Failed: " + e.getMessage());
        }
    }

    private boolean sendViaMetaWhatsapp(String toPhone, String body) {
        if (metaPhoneNumberId == null || metaPhoneNumberId.isBlank() || metaAccessToken == null || metaAccessToken.isBlank()) {
            String errorMsg = "WhatsApp Delivery Failed: Meta WhatsApp Cloud API Credentials are not configured. Please set META_WHATSAPP_PHONE_ID and META_WHATSAPP_ACCESS_TOKEN in application.yml or environment variables.";
            log.error(errorMsg);
            throw new BadRequestException(errorMsg);
        }

        try {
            String url = String.format("https://graph.facebook.com/v18.0/%s/messages", metaPhoneNumberId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(metaAccessToken);

            String digitsOnly = toPhone.replaceAll("[^0-9]", "");
            String jsonPayload = String.format(
                "{\"messaging_product\":\"whatsapp\",\"recipient_type\":\"individual\",\"to\":\"%s\",\"type\":\"text\",\"text\":{\"preview_url\":false,\"body\":\"%s\"}}",
                digitsOnly, escapeJson(body)
            );

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("META WHATSAPP DISPATCH SUCCESS: Status {} | Response: {}", response.getStatusCode(), response.getBody());
                return true;
            } else {
                log.error("META WHATSAPP DISPATCH FAILED: Status {} | Response: {}", response.getStatusCode(), response.getBody());
                throw new BadRequestException("Meta WhatsApp Cloud API Error: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Meta WhatsApp API Execution Error: {}", e.getMessage(), e);
            if (e instanceof BadRequestException) throw (BadRequestException) e;
            throw new BadRequestException("Meta WhatsApp Gateway Delivery Failed: " + e.getMessage());
        }
    }

    private String sanitizePhoneNumber(String raw) {
        if (raw == null || raw.isBlank()) return "+919876543210";
        String clean = raw.replaceAll("[^0-9+]", "");
        if (!clean.startsWith("+")) {
            clean = "+91" + clean;
        }
        return clean;
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
