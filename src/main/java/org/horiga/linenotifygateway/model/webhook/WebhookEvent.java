package org.horiga.linenotifygateway.model.webhook;

import java.util.Map;

public interface WebhookEvent {
    String getEventName();

    Map<String, Object> getMessage();

    Object getProperty(String key);

    Object getProperty(String key, Object defaultValue);
}
