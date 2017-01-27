package org.horiga.linenotifygateway.service;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.horiga.linenotifygateway.model.Notify;
import org.horiga.linenotifygateway.support.MustacheMessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebhookServiceDispatcher {

    private final ObjectMapper mapper;

    private final MustacheMessageBuilder messageBuilder;

    private final NotifyService notifyService;

    @Autowired
    public WebhookServiceDispatcher(ObjectMapper mapper,
                                    MustacheMessageBuilder messageBuilder,
                                    NotifyService notifyService) {
        this.mapper = mapper;
        this.messageBuilder = messageBuilder;
        this.notifyService = notifyService;
    }

    public void dispatch(String service, Map<String, Object> message, HttpServletRequest request) {
        try {
            printWebhookEvent(service, message, request);
            // TODO: refactoring
            notifyService.execute(
                    new Notify("github.com", messageBuilder.build("pull_request.mustache", message),
                               "", ""), ImmutableList.of(request.getParameter("notify_token")));
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
