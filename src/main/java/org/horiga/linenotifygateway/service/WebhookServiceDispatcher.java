package org.horiga.linenotifygateway.service;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebhookServiceDispatcher {

    private final ObjectMapper mapper;

    public WebhookServiceDispatcher(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void dispatch(String service, Map<String, Object> message, HttpServletRequest request) {
        try {
            printWebhookEvent(service, message, request);



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
