package org.horiga.linenotifygateway.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.horiga.linenotifygateway.config.GitHubLineNotifyGatewayProperties;
import org.horiga.linenotifygateway.model.Notify;
import org.horiga.linenotifygateway.support.MustacheMessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GitHubWebhookHandler extends WebhookHandler {

    private final NotifyService notifyService;
    private final List<String> eventTypes;
    private final MustacheMessageBuilder messageBuilder;
    private final ObjectMapper mapper;

    @SuppressWarnings("unused")
    public enum EventTypes {
        commit_comment("commit_comment", ""),
        create("create", ""),
        delete("delete", ""),
        deployment("deployment", ""),
        deployment_status("deployment_status", ""),
        fork("fork", ""),
        gollum("gollum", ""),
        issue_comment("issue_comment", ""),
        issues("issues", ""),
        label("label", ""),
        member("member", ""),
        membership("membership", ""),
        milestone("milestone", ""),
        organization("organization", ""),
        page_build("page_build", ""),
        project_card("project_card", ""),
        project_column("project_column", ""),
        project("project", ""),
        public_event("public", ""),
        pull_request_review_comment("pull_request_review_comment", ""),
        pull_request_review("pull_request_review", "1,134"),
        pull_request("pull_request", "1,106"),
        push("push", ""),
        repository("repository", ""),
        release("release", "2,144"),
        status("status", ""),
        team("team", ""),
        team_add("team_add", ""),
        watch("watch", ""),
        undefined("watch", "")
        ;

        private final String defaultSticker;
        private final String eventName;

        EventTypes(String eventName, String defaultSticker) {
            this.eventName = eventName;
            this.defaultSticker = defaultSticker;
        }

        public static Optional<EventTypes> eventType(String name) {
            return Arrays.stream(EventTypes.values()).filter(e -> e.eventName.equals(name)).findFirst();
        }
    }

    @Autowired
    public GitHubWebhookHandler(
            NotifyService notifyService,
            GitHubLineNotifyGatewayProperties properties,
            MustacheMessageBuilder messageBuilder,
            ObjectMapper mapper) {
        super("github");
        this.notifyService = notifyService;
        eventTypes = ImmutableList.copyOf(properties.getEventTypes());
        this.messageBuilder = messageBuilder;
        this.mapper = mapper;
    }

    @Override
    public void handleMessage(String tokenKey,
                              Map<String, Object> message,
                              HttpServletRequest request) throws Exception {
        final String thisEvent = request.getHeader("X-GitHub-Event");
        if (!supportEvents(thisEvent)) {
            log.info("Unsupported event has received. X-GitHub-Event: {}, payload: {}",
                     thisEvent, mapper.writeValueAsString(message));
            return;
        }
        notifyService.send(new Notify(getWebhookServiceName(),
                                         buildMessage(thisEvent, message, request),
                                         "",
                                         "",
                                         tokenKey,
                                         StringUtils.defaultString(request.getParameter("notify_token"), ""),
                                         getStickerWithGitHubEventName(thisEvent)));
    }

    protected boolean supportEvents(String event) {
        // TODO: change support GitHub event management with CMS.
        return StringUtils.isNotBlank(event) && eventTypes.contains(event);
    }

    protected String getStickerWithGitHubEventName(String event) {
        // TODO: change sticker management with CMS.
        return EventTypes.eventType(event).orElse(EventTypes.undefined).defaultSticker;
    }

    protected String buildMessage(
            String eventType,
            Map<String, Object> message,
            HttpServletRequest request
    ) {
        // TODO: change template management with CMS.
        return messageBuilder.build("github/" + eventType + ".mustache", message);
    }
}
