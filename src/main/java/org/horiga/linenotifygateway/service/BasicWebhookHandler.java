package org.horiga.linenotifygateway.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BasicWebhookHandler extends WebhookHandler {

    @Autowired
    public BasicWebhookHandler() {
        super("basic");
    }

    @Override
    public void handleMessage(String tokenKey, Map<String, Object> message, HttpServletRequest request) {
        log.warn("handle unknown webhook payload. {}", message);
    }
}
