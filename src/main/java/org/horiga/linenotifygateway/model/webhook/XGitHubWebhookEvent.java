package org.horiga.linenotifygateway.model.webhook;

import java.util.Map;

import com.google.common.collect.Maps;

public class XGitHubWebhookEvent extends BasicWebhookEvent {

    public interface GithubWebhookEventNames {
        String PULL_REQUEST = "";
    }

    private static Map<String, Object> empty = Maps.newHashMap();

    public XGitHubWebhookEvent(String name, Map<String, Object> message) {
        super(name, message);
    }
}
