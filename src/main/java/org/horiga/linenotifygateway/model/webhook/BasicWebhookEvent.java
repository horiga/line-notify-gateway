package org.horiga.linenotifygateway.model.webhook;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BasicWebhookEvent implements WebhookEvent {

    protected String eventName;

    protected Map<String, Object> message;

    @Override
    public Object getProperty(String key) {
        return message.get(key);
    }

    @Override
    public Object getProperty(String key, Object defaultValue) {
        return message.getOrDefault(key, defaultValue);
    }
}
