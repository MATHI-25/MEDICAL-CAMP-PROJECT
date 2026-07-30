package com.mediq.service;

public interface WhatsappGatewayService {

    boolean sendPhysicalWhatsapp(String recipientPhone, String messageText);
}
