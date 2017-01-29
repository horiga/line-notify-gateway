package org.horiga.linenotifygateway.service;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.horiga.linenotifygateway.config.LineNotifyGatewayProperties;
import org.horiga.linenotifygateway.controller.NotifyGatewayRestController.ResponseMessage;
import org.horiga.linenotifygateway.model.Notify;
import org.horiga.linenotifygateway.support.MustacheMessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotifyService {

    private final String endpointURI;

    private final String accessToken;

    private final RestTemplate restTemplate;

    private final GaugeService gaugeService;

    @Autowired
    public NotifyService(
            @Qualifier("lineNotifyRestTemplate") RestTemplate restTemplate,
            LineNotifyGatewayProperties properties,
            GaugeService gaugeService) {
        this.restTemplate = restTemplate;
        endpointURI = properties.getEndpointUri();
        accessToken = properties.getPersonalAccessToken();
        this.gaugeService = gaugeService;
    }

    public void execute(Notify notify) {
        sendNotify(notify, accessToken);
    }

    public void execute(Notify notify, List<String> accessTokens) {
        accessTokens.parallelStream().forEach(token -> sendNotify(notify, token));
    }

    private void sendNotify(Notify notify, String accessToken) {
        try {
            // TODO async
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            markAsRateLimit(restTemplate.exchange(
                    new URI(endpointURI),
                    HttpMethod.POST,
                    new HttpEntity<>(valueMap(notify), headers),
                    ResponseMessage.class));
        } catch (Exception ignore) {
            log.error("Failed to LINE Notify API execution: {}", valueMap(notify));
        }
    }

    private void markAsRateLimit(ResponseEntity<ResponseMessage> response) {
        final Map<String, Object> rateLimits = Maps.newLinkedHashMap();
        response.getHeaders().entrySet()
                .stream()
                .filter(header -> header.getKey().startsWith("X-RateLimit-"))
                .forEach(header -> rateLimits.put(header.getKey().toLowerCase(), header.getValue().get(0)));
        log.info("LINE Notify API Rate Limit: accessToken:{}, rateLimit: {}", accessToken, rateLimits);
        ImmutableSet<String> rateLimitKeys = ImmutableSet.of("Limit", "Remaining", "ImageLimit",
                                                             "ImageRemaining");
        rateLimitKeys.forEach(key -> gaugeService.submit("LINENotify.RateLimits." + key,
                                                     Double.parseDouble((String) rateLimits
                                    .getOrDefault(("X-RateLimit-" + key).toLowerCase(), "0"))));
    }

    public MultiValueMap<String, String> valueMap(Notify notify) {
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();

        String _message = notify.getMessage();
        if (_message.length() >= 1000) {
            _message = _message.substring(0, 990) + " ...";
        }
        valueMap.add("message", _message);
        if (StringUtils.isNotBlank(notify.getThumbnailUri())
            && (notify.getThumbnailUri().startsWith("http://")
                || notify.getThumbnailUri().startsWith("https://"))) {
            valueMap.add("imageThumbnail", notify.getThumbnailUri());
        }
        if (StringUtils.isNotBlank(notify.getImageUri())
            && (notify.getImageUri().startsWith("http://")
                || notify.getImageUri().startsWith("https://"))) {
            valueMap.add("imageFullsize", notify.getImageUri());
        }
        if (StringUtils.isNotBlank(notify.getStickerPackageId())
            && StringUtils.isNumeric(notify.getStickerPackageId())) {
            valueMap.add("stickerPackageId", notify.getStickerPackageId());
        }
        if (StringUtils.isNotBlank(notify.getStickerId())
            && StringUtils.isNumeric(notify.getStickerId())) {
            valueMap.add("stickerId", notify.getStickerId());
        }
        return valueMap;
    }

}
