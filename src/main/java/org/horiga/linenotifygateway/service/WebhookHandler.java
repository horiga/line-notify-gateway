package org.horiga.linenotifygateway.service;

import lombok.Getter;

public abstract class WebhookHandler {

    @Getter
    private final String serviceName;

    private final NotifyService notifyService;

    protected WebhookHandler(String serviceName, NotifyService notifyService) {
        this.serviceName = serviceName;
        this.notifyService = notifyService;
    }


}
