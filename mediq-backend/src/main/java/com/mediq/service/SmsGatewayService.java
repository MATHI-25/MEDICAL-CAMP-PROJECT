package com.mediq.service;

public interface SmsGatewayService {

    boolean sendPhysicalSms(String recipientPhone, String messageText);
}
