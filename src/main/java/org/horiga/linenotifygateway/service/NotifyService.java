package org.horiga.linenotifygateway.service;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.horiga.linenotifygateway.config.LineNotifyGatewayProperties;
import org.horiga.linenotifygateway.controller.NotifyGatewayRestController.ResponseMessage;
import org.horiga.linenotifygateway.model.Notify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotifyService {

    private final String endpointURI;

    private final String accessToken;

    private final RestTemplate restTemplate;

    @Autowired
    public NotifyService(
            @Qualifier("lineNotifyRestTemplate") RestTemplate restTemplate,
            LineNotifyGatewayProperties properties
    ) {
        this.restTemplate = restTemplate;
        endpointURI = properties.getEndpointUri();
        accessToken = properties.getPersonalAccessToken();
    }

    public void execute(Notify notify) {
        sendNotify(notify, accessToken);
    }

    public void execute(Notify notify, List<String> accessTokens) {
        accessTokens.parallelStream().forEach(token -> sendNotify(notify, token));
    }

    private void sendNotify(Notify notify, String accessToken) {
        // TODO: asynchronous
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            ResponseEntity<ResponseMessage> response = restTemplate.exchange(new URI(endpointURI),
                                                                             HttpMethod.POST,
                                                                             new HttpEntity<>(notify.valueMap(),
                                                                                              headers),
                                                                             ResponseMessage.class);
            final Map<String, Object> rateLimits = Maps.newLinkedHashMap();
            response.getHeaders().entrySet()
                    .stream()
                    .filter(header -> header.getKey().startsWith("X-RateLimit"))
                    .forEach(header -> rateLimits.put(header.getKey(), header.getValue().get(0)));

            log.info("LINE Notify API Rate Limit: accessToken:{}, rateLimit: {}", accessToken, rateLimits);

            markAsRateLimit(rateLimits);

        } catch (Exception ignore) {
            log.error("Failed to LINE Notify API execution: {}", notify.valueMap());
        }
    }

    private void markAsRateLimit(Map<String, Object> rateLimits) {
        // TODO
    }
}
