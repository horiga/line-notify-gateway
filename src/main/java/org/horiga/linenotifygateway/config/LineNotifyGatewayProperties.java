package org.horiga.linenotifygateway.config;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "line-notify")
@Data
@Component
public class LineNotifyGatewayProperties {

    @NotNull
    private String endpointUri = "https://notify-api.line.me";

    private String mustacheTemplatePath;

    @NotNull
    @Valid
    @NestedConfigurationProperty
    private HttpClientProperties httpClient;

    @Data
    public static class HttpClientProperties {
        @Min(1000)
        private Integer connectTimeout = 3000;

        @Min(1000)
        private Integer readTimeout = 5000;

        @Min(1000)
        private Integer writeTimeout = 5000;

        @NotNull
        private String logLevel = "BASIC";
    }
}
