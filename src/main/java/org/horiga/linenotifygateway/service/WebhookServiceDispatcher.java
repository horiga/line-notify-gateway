package org.horiga.linenotifygateway.service;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.horiga.linenotifygateway.support.MustacheMessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
        webhookHandlers.entrySet().forEach(entry -> log.info("Registered Webhook => serviceName: {}, handler: {}", entry.getKey(), entry.getValue()));
    }

    public void dispatch(String service, Map<String, Object> message, HttpServletRequest request) {
        try {
            printWebhookEvent(service, message, request);
            webhookHandlers.getOrDefault(service, defaultWebhookHandler).handleMessage(message, request);
        } catch (Exception e) {
            log.error("handle webhook event message dispatcher error!", e);
        }
    }

    private void printWebhookEvent(String service, Map<String, Object> message, HttpServletRequest request)
            throws Exception {
        final StringBuilder parameters = new StringBuilder();
        request.getParameterMap().entrySet()
               .forEach(entry -> parameters.append(entry.getKey())
                                           .append('=')
                                           .append(Joiner.on(", ").skipNulls().join(entry.getValue())));
        final Enumeration<String> headerNames = request.getHeaderNames();
        final StringBuilder headers = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            headers.append(headerName).append(':').append(request.getHeader(headerName)).append(", ");
        }
        log.info("handle webhook events: service:{}, request.params:{}, request.headers:{} message:{}",
                 service,
                 parameters,
                 headers,
                 mapper.writeValueAsString(message));
    }
}
