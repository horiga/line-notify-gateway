package org.horiga.linenotifygateway.config;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "notify.line")
@Data
@Component
public class Settings {

    @NotBlank
    private String personalAccessToken;

    @NotBlank
    private String endpointUri = "https://notify-api.line.me";

    private int connectTimeout = 3000;

    private int readTimeout = 5000;
}
