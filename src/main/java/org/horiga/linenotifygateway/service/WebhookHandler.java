package org.horiga.linenotifygateway.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.Getter;

public abstract class WebhookHandler {

    @Getter
    private final String webhookServiceName;

    protected WebhookHandler(String webhookServiceName) {
        this.webhookServiceName = webhookServiceName;
    }

    public abstract void handleMessage(String tokenKey,
                                       Map<String, Object> payload,
                                       HttpServletRequest request) throws Exception;
}
