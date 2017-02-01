package org.horiga.linenotifygateway.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BasicWebhookHandler extends WebhookHandler {

    public BasicWebhookHandler() {
        super("basic");
    }

    @Override
    public void handleMessage(String tokenKey, Map<String, Object> message, HttpServletRequest request) {
        log.warn("handle unknown webhook payload. {}", message);
    }
}
