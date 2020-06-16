package org.horiga.linenotifygateway.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "plugins.line-notify.github")
@Data
@Component
public class GithubLineNotifyGatewayProperties {

    private List<String> eventTypes;

    private String reviewKeyword = "review wanted";

    private String notifyAccessToken = "";
}
