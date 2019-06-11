package org.horiga.linenotifygateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.horiga.linenotifygateway.service.*;
import org.horiga.linenotifygateway.support.MustacheMessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@Configuration
@Slf4j
public class LineNotifyGatewayConfig {

    private final LineNotifyGatewayProperties properties;

    private final GithubLineNotifyGatewayProperties githubProperties;

    @Autowired
    public LineNotifyGatewayConfig(LineNotifyGatewayProperties properties,
                                   GithubLineNotifyGatewayProperties githubProperties) {
        this.properties = properties;
        this.githubProperties = githubProperties;
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
                        final StringBuilder s = new StringBuilder();
                        response.getHeaders().entrySet()
                                .forEach(header -> s.append(header.getKey())
                                                    .append(": ")
                                                    .append(Joiner.on(",")
                                                                  .skipNulls()
                                                                  .join(header.getValue())));
                        log.warn("Receive LINE Notify API Error, [{} {}], HTTP Header: {}",
                                 response.getStatusCode().value(),
                                 response.getStatusText(), s);
                        super.handleError(response);
                    }
                })
                .build();
    }

    @Bean
    MustacheMessageBuilder mustacheMessageBuilder() {
        return new MustacheMessageBuilder(properties.getMustacheTemplatePath());
    }

    @Bean
    GithubWebhookHandler githubWebhookHandler(NotifyService notifyService, ObjectMapper mapper) {
        return new GithubWebhookHandler(notifyService, githubProperties, mustacheMessageBuilder(), mapper);
    }

    @Bean
    AlertManagerWebhookHandler alertManagerWebhookHandler(NotifyService notifyService, ObjectMapper mapper) {
        return new AlertManagerWebhookHandler(notifyService, properties, mustacheMessageBuilder(), mapper);
    }

    @Bean
    WebhookServiceDispatcher webhookServiceDispatcher(
            ObjectMapper mapper, GithubWebhookHandler githubWebhookHandler, AlertManagerWebhookHandler alertManagerWebhookHandler) {
        WebhookHandler defaultWebhookHandler = new BasicWebhookHandler();
        final Map<String, WebhookHandler> webhookHandlers = Maps.newHashMap();
        webhookHandlers.put(githubWebhookHandler.getWebhookServiceName(), githubWebhookHandler);
        webhookHandlers.put(alertManagerWebhookHandler.getWebhookServiceName(), alertManagerWebhookHandler);
        return new WebhookServiceDispatcher(mapper, webhookHandlers, defaultWebhookHandler);
    }

}
