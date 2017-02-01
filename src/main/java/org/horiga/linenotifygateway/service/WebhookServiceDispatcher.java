package org.horiga.linenotifygateway.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.horiga.linenotifygateway.entity.ServiceEntity;
import org.horiga.linenotifygateway.repository.ServiceRepository;
import org.horiga.linenotifygateway.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebhookServiceDispatcher {

    private final ObjectMapper mapper;

    private final Map<String, WebhookHandler> webhookHandlers;

    private final WebhookHandler defaultWebhookHandler;

    private final ServiceRepository serviceRepository;

    private final TokenRepository tokenRepository;

    @Autowired
    public WebhookServiceDispatcher(ObjectMapper mapper,
                                    @Qualifier("webhookHandlers") Map<String, WebhookHandler> webhookHandlers,
                                    @Qualifier("defaultWebhookHandler") WebhookHandler defaultWebhookHandler,
                                    ServiceRepository serviceRepository,
                                    TokenRepository tokenRepository) {
        this.mapper = mapper;
        this.webhookHandlers = webhookHandlers;
        this.defaultWebhookHandler = defaultWebhookHandler;
        this.serviceRepository = serviceRepository;
        this.tokenRepository = tokenRepository;
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
            if (Objects.isNull(serviceRepository.findById(service))) {
                log.error("unsupported service, {}", service);
            }
            webhookHandlers.getOrDefault(service,
                                         defaultWebhookHandler).handleMessage(tokenKey, messageBody, request);
        } catch (Exception e) {
            log.error("handle webhook event message dispatcher error!", e);
        }
    }

    public Map<String, Object> getAvailableDispatcher() {
        Collection<ServiceEntity> services = serviceRepository.findAll();
        Map<String, Object> results = Maps.newTreeMap();
        results.put("count", services.size());
        List<Map<String, Object>> items = Lists.newLinkedList();
        services.forEach(s -> {
            Map<String, Object> data = Maps.newTreeMap();
            data.put("name", s.getService());
            data.put("class", webhookHandlers.get(s.getService()).getClass().getCanonicalName());
            data.put("description", s.getDescription());
            data.put("accessToken", tokenRepository.findByServiceId(s.getService()));
        });
        results.put("data", items);
        return results;
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
