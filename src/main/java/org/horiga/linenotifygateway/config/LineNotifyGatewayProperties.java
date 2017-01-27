package org.horiga.linenotifygateway.config;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "notify.line")
@Data
@Component
public class LineNotifyGatewayProperties {

    @NotBlank
    private String personalAccessToken;

    @NotBlank
    private String endpointUri = "https://notify-api.line.me";

    @Min(1000)
    private int connectTimeout = 3000;

    @Min(1000)
    private int readTimeout = 5000;

    private String mustacheTemplatePath;
}
