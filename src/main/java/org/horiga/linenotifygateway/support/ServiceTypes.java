package org.horiga.linenotifygateway.support;

public enum ServiceTypes {

    DIRECT("Direct"),
    ALERT("Alerts"),
    WEBHOOK("Webhook");

    private final String displayName;

    ServiceTypes(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
