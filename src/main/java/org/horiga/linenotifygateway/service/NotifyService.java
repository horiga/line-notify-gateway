package org.horiga.linenotifygateway.service;

import java.net.URI;
import java.util.List;

import org.horiga.linenotifygateway.config.Settings;
import org.horiga.linenotifygateway.controller.NotifyGatewayRestController.ResponseMessage;
import org.horiga.linenotifygateway.model.Notify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
            Settings settings
    ) {
        this.restTemplate = restTemplate;
        endpointURI = settings.getEndpointUri();
        accessToken = settings.getPersonalAccessToken();
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
            restTemplate.exchange(new URI(endpointURI),
                                  HttpMethod.POST,
                                  new HttpEntity<>(notify.valueMap(), headers), ResponseMessage.class);
        } catch (Exception ignore) {
            log.error("Failed to LINE Notify API execution: {}", notify.valueMap());
        }
    }
}
