package org.horiga.linenotifygateway.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.horiga.linenotifygateway.config.GithubLineNotifyGatewayProperties;
import org.horiga.linenotifygateway.model.Notify;
import org.horiga.linenotifygateway.support.MustacheMessageBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GithubWebhookHandler extends WebhookHandler {

    private final NotifyService notifyService;
    private final List<String> eventTypes;
    private final String accessToken;
    private final MustacheMessageBuilder messageBuilder;
    private final ObjectMapper mapper;
    private final String reviewKeyword;

    public GithubWebhookHandler(
            NotifyService notifyService,
            GithubLineNotifyGatewayProperties properties,
            MustacheMessageBuilder messageBuilder,
            ObjectMapper mapper) {
        super("github");
        this.notifyService = notifyService;
        eventTypes = ImmutableList.copyOf(properties.getEventTypes());
        accessToken = properties.getNotifyAccessToken();
        this.messageBuilder = messageBuilder;
        this.mapper = mapper;
        reviewKeyword = properties.getReviewKeyword();
    }

    @Override
    public void handleMessage(Map<String, Object> message, HttpServletRequest request) throws Exception {
        final String event = request.getHeader("X-GitHub-Event");
        if (StringUtils.isBlank(event)
            || !hasNotifyEvent(event)) {
            log.warn("Unsupported event has received. X-GitHub-Event: {}, payload: {}",
                     event, mapper.writeValueAsString(message));
            return;
        }

        final String accessToken = StringUtils.isNotBlank(request.getParameter("notify_token")) ?
                             request.getParameter("notify_token") : this.accessToken;
        if (StringUtils.isNotBlank(accessToken)) {
            Notify notify = new Notify("github", buildMessage(event, message, request), "", "");
            notifyService.execute(applySticker(event, message, notify),
                                  Splitter.on(",")
                                          .omitEmptyStrings()
                                          .trimResults()
                                          .splitToList(accessToken));
        }
    }

    protected boolean hasNotifyEvent(String event) {
        return eventTypes.contains(event);
    }

    protected Notify applySticker(String event, Map<String, Object> message, Notify notify) {

        if(message.containsKey("_sticker")) {
            return notify.addSticker((String)message.get("_sticker"));
        }

        if ("pull_request".equals(event)
            && "opened".equals(message.getOrDefault("action", "NONE"))) {
            return notify.addSticker("1,106");
        }

        if ("release".equals(event)) {
            return notify.addSticker("2,144");
        }

        return notify;
    }

    protected String buildMessage(
            String eventType,
            Map<String, Object> message,
            HttpServletRequest request
    ) {
        // type: pull_request, action: labeled
        String templateName = eventType;

        // WIP:
        if ("pull_request".equalsIgnoreCase(eventType)
            && "labeled".equals(message.getOrDefault("action", "NONE"))) {
            templateName = eventType + "-action_labeled";
        }
        if ("issues".equalsIgnoreCase(eventType)
            && "labeled".equals(message.getOrDefault("action", "NONE"))) {
            templateName = eventType + "-action_labeled";
        }
        if("pull_request_review".equalsIgnoreCase(eventType)
            && "submitted".equals(message.getOrDefault("action", "NONE"))) {
            templateName = eventType + "-action_submitted";
        }

        return messageBuilder.build("github/" + templateName + ".mustache", message);
    }
}
