package org.horiga.linenotifygateway.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.horiga.linenotifygateway.config.LineNotifyGatewayProperties.HttpClientProperties;
import org.horiga.linenotifygateway.service.GitHubWebhookHandler;
import org.horiga.linenotifygateway.service.WebhookHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

@Configuration
@Slf4j
public class LineNotifyGatewayConfig {

    private final LineNotifyGatewayProperties properties;

    @Autowired
    public LineNotifyGatewayConfig(LineNotifyGatewayProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "lineNotifyRestTemplate")
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(properties.getHttpClient().getConnectTimeout())
                .setReadTimeout(properties.getHttpClient().getReadTimeout())
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
                        log.warn("Receive LINE NotifyMessage API Error, [{} {}], HTTP Header: {}",
                                 response.getStatusCode().value(),
                                 response.getStatusText(), s);
                        super.handleError(response);
                    }
                })
                .build();
    }

    @Bean(name = "webhookHandlers")
    Map<String, WebhookHandler> webhookHandlers(GitHubWebhookHandler githubWebhookHandler) {
        log.error("Bean: webhookHandlers creation.");
        final Map<String, WebhookHandler> webhookHandlers = Maps.newHashMap();
        webhookHandlers.put(githubWebhookHandler.getWebhookServiceName(), githubWebhookHandler);
        return webhookHandlers;
    }

    @Bean
    OkHttpClient okHttpClient() {
        final HttpClientProperties props = properties.getHttpClient();
        return new OkHttpClient.Builder()
                .connectTimeout(props.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(props.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(props.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .followRedirects(false)
                .addInterceptor(
                        new HttpLoggingInterceptor()
                                .setLevel(Level.valueOf(props.getLogLevel().toUpperCase()))
                )
                .addInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder()
                             .header(HttpHeaders.USER_AGENT, "LineNotifyGateway")
                             .build()))
                .build();
    }

}
