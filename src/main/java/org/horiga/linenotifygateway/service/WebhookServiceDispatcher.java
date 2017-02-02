package org.horiga.linenotifygateway.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.horiga.linenotifygateway.repository.ServiceRepository;
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

    private final ServiceRepository serviceRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public WebhookServiceDispatcher(ObjectMapper mapper,
                                    @NotNull @Qualifier("webhookHandlers")
                                            Map<String, WebhookHandler> webhookHandlers,
                                    @NotNull ServiceRepository serviceRepository) {
        this.mapper = mapper;
        this.webhookHandlers = webhookHandlers;
        this.serviceRepository = serviceRepository;
        webhookHandlers.entrySet()
                       .forEach(entry -> log.info("Registered Webhook => serviceName: {}, handler: {}",
                                                  entry.getKey(), entry.getValue()));
    }

    public void dispatch(String service,
                         String tokenKey,
                         Map<String, Object> payload,
                         HttpServletRequest request) {
        try {
            printWebhookEvent(service, tokenKey, payload, request);
            if (Objects.isNull(serviceRepository.findById(service))) {
                log.error("unsupported service, {}", service);
            }
            if (webhookHandlers.containsKey(service)) {
                webhookHandlers.get(service).handleMessage(tokenKey, payload, request);
            } else {
                log.warn("unknown webhook message received. {}", service);
            }
        } catch (Exception e) {
            log.error("handle webhook event message dispatcher error!", e);
        }
    }

    public Map<String, Object> getAvailableDispatcher() {
        List<Map<String, Object>> items = Lists.newLinkedList();
        serviceRepository.findAll()
                         .forEach(s -> {
            if (webhookHandlers.containsKey(s.getService())) {
                Map<String, Object> data = Maps.newTreeMap();
                data.put("name", s.getService());
                data.put("class", webhookHandlers.get(s.getService()).getClass().getCanonicalName());
                data.put("description", s.getDescription());
                items.add(data);
            }
        });
        Map<String, Object> results = Maps.newTreeMap();
        results.put("count", items.size());
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
