package org.horiga.linenotifygateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.horiga.linenotifygateway.config.LineNotifyGatewayProperties;
import org.horiga.linenotifygateway.model.Notify;
import org.horiga.linenotifygateway.support.MustacheMessageBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class AlertManagerWebhookHandler extends WebhookHandler {

    private final NotifyService notifyService;
    private final String accessToken;
    private final MustacheMessageBuilder messageBuilder;
    private final ObjectMapper mapper;


    public AlertManagerWebhookHandler(NotifyService notifyService, LineNotifyGatewayProperties properties, MustacheMessageBuilder messageBuilder, ObjectMapper mapper) {
        super("alertmanager");
        this.notifyService = notifyService;
        this.accessToken = properties.getPersonalAccessToken();
        this.messageBuilder = messageBuilder;
        this.mapper = mapper;
    }


    @Override
    public void handleMessage(Map<String, Object> message, HttpServletRequest request) throws Exception {
        final String accessToken = StringUtils.isNotBlank(request.getParameter("notify_token")) ?
                request.getParameter("notify_token") : this.accessToken;
        if (StringUtils.isNotBlank(accessToken)) {
            Notify notify = new Notify("github", buildMessage(message, request), "", "");
            notifyService.execute(applySticker(message, notify),
                    Splitter.on(",")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(accessToken));
        }
    }

    protected Notify applySticker(Map<String, Object> message, Notify notify) {
        if ("firing".equals(message.getOrDefault("status", "NONE"))) {
            return notify.addSticker("2,24"); // firing
        }

        if ("resolved".equals(message.getOrDefault("status", "NONE"))) {
            return notify.addSticker("1,114"); // resolved
        }

        return notify;
    }

    protected String buildMessage(Map<String, Object> message, HttpServletRequest request) {

        String templateName = "firing";

        if ("firing".equals(message.getOrDefault("status", "NONE"))) {
            templateName = "firing";
        }

        if ("resolved".equals(message.getOrDefault("status", "NONE"))) {
            templateName = "resolved";
        }

        return messageBuilder.build("alertmanager/" + templateName + ".mustache", message);
    }
}

