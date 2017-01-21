package org.horiga.linenotifygateway.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class LineNotifyGatewayConfig {

    private final Settings properties;

    @Autowired
    public LineNotifyGatewayConfig(Settings properties) {
        this.properties = properties;
    }

    @Bean(name = "lineNotifyRestTemplate")
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(properties.getConnectTimeout())
                .setReadTimeout(properties.getReadTimeout())
                .additionalMessageConverters(new MappingJackson2HttpMessageConverter())
                .errorHandler(new DefaultResponseErrorHandler() {
                    @SuppressWarnings("RedundantThrowsDeclaration")
                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {
                        try {
                            super.handleError(response);
                        } catch (RestClientResponseException e) {
                            log.error("LINE Notify API Error: [{}] {}, http-response-headers: {}",
                                      e.getRawStatusCode(),
                                      e.getResponseBodyAsString(),
                                      e.getResponseHeaders().values(),
                                      e);
                            throw e;
                        }
                    }
                })
                .build();
    }

}
