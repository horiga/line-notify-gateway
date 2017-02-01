package org.horiga.linenotifygateway.service;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebhookServiceDispatcher {

    private final ObjectMapper mapper;

    private final Map<String, WebhookHandler> webhookHandlers;

    private final WebhookHandler defaultWebhookHandler;

    public WebhookServiceDispatcher(ObjectMapper mapper,
                                    Map<String, WebhookHandler> webhookHandlers,
                                    WebhookHandler defaultWebhookHandler) {
        this.mapper = mapper;
        this.webhookHandlers = webhookHandlers;
        this.defaultWebhookHandler = defaultWebhookHandler;
        webhookHandlers.entrySet()
                       .forEach(entry -> log.info("Registered Webhook => serviceName: {}, handler: {}",
                                                  entry.getKey(), entry.getValue()));
    }

    public void dispatch(String service,
                         String tokenKey,
                         Map<String, Object> messageBody,
                         HttpServletRequest request) {
        try {
            printWebhookEvent(service, tokenKey, messageBody, request);
            webhookHandlers.getOrDefault(service,
                                         defaultWebhookHandler).handleMessage(tokenKey, messageBody, request);
        } catch (Exception e) {
            log.error("handle webhook event message dispatcher error!", e);
        }
    }

    private void printWebhookEvent(String service, String tokenKey,
                                   Map<String, Object> messageBody, HttpServletRequest request)
            throws Exception {
        final StringBuilder parameters = new StringBuilder();
        request.getParameterMap().entrySet()
               .forEach(entry -> parameters.append(entry.getKey())
                                           .append('=')
                                           .append(Joiner.on(", ").skipNulls().join(entry.getValue())));
        final StringBuilder headers = new StringBuilder();
        Collections.list(request.getHeaderNames())
                   .forEach(header ->
                                    headers.append(header)
                                           .append(':')
                                           .append(request.getHeader(header)).append(", "));
        log.info("handle webhook events: service:{}/{}, request.params:{}, request.headers:{} message:{}",
                 service,
                 tokenKey,
                 parameters,
                 headers,
                 mapper.writeValueAsString(messageBody));
    }
}
