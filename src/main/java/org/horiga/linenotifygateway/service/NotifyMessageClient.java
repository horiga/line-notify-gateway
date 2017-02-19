package org.horiga.linenotifygateway.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.horiga.linenotifygateway.config.LineNotifyGatewayProperties;
import org.horiga.linenotifygateway.model.NotifyMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Component
public class NotifyMessageClient {

    private static final MediaType
            MEDIA_TYPE_FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private static final ImmutableSet<String> LINE_NOTIFY_RATE_LIMIT_NAMES =
            ImmutableSet.of("Limit", "Remaining", "ImageLimit", "ImageRemaining");

    private final String endpointURI;

    private final OkHttpClient okHttpClient;

    private final GaugeService gaugeService;

    @Autowired
    public NotifyMessageClient(
            OkHttpClient okHttpClient,
            LineNotifyGatewayProperties properties,
            GaugeService gaugeService) {
        this.okHttpClient = okHttpClient;
        endpointURI = properties.getEndpointUri();
        this.gaugeService = gaugeService;
    }

    public void send(NotifyMessage notifyMessage) {
        execute(notifyMessage);
    }

    private void execute(NotifyMessage notifyMessage) {
        final String content = Joiner.on("&").withKeyValueSeparator("=").join(valueMap(notifyMessage));
        notifyMessage.getAccessToken().forEach(accessToken -> {
            try {
                okHttpClient.newCall(buildRequest(accessToken, content)).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        try {
                            log.warn("Failed to access {}", endpointURI, e);
                            if (call.isExecuted()) { call.cancel(); }
                        } catch (Exception ignore) {}
                    }

                    @SuppressWarnings("RedundantThrowsDeclaration")
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            if (response.code() / 100 != 2) {
                                StringBuilder sb = new StringBuilder();
                                response.headers().names()
                                        .forEach(name -> sb.append(name)
                                                           .append(response.header(name))
                                                           .append(','));
                                log.warn("Received HTTP error: http.status={}, http.headers={}, http.body={}",
                                         response.code(), sb, response.body().string());
                            }
                            markAsRateLimit(accessToken, response);
                        } catch (Exception ignore) {}
                    }
                });
            } catch (Exception ignore) {
                log.error("Failed to LINE NotifyMessage API execution: {}", valueMap(notifyMessage));
            }
        });
    }

    private Request buildRequest(final String accessToken, final String content) {
        final Request.Builder request = new Request.Builder();
        request.url(endpointURI)
               .header("Authorization", "Bearer " + accessToken)
               .post(RequestBody.create(MEDIA_TYPE_FORM_URLENCODED, content));
        return request.build();
    }

    private void markAsRateLimit(final String accessToken, final Response response) {
        final Map<String, Object> rateLimits = Maps.newLinkedHashMap();
        response.headers().names()
                .stream()
                .filter(name -> name.startsWith("X-RateLimit-"))
                .forEach(name -> rateLimits.put(name.toLowerCase(), response.header(name)));
        log.info("LINE NotifyMessage API Rate Limit: accessToken:{}, rateLimit: {}", accessToken, rateLimits);
        LINE_NOTIFY_RATE_LIMIT_NAMES.forEach(key -> gaugeService.submit(
                "LINENotify.RateLimits." + key + '.' + accessToken,
                Double.parseDouble(
                        (String) rateLimits.getOrDefault(("X-RateLimit-" + key).toLowerCase(), "0"))));
    }

    private static Map<String, String> valueMap(final NotifyMessage notifyMessage) {
        Map<String, String> valueMap = new HashMap<>();
        String _message = notifyMessage.getMessage();
        if (_message.length() >= 1000) {
            _message = _message.substring(0, 990) + "...";
        }
        valueMap.put("message", _message);
        if (StringUtils.isNotBlank(notifyMessage.getThumbnailUri())
            && (notifyMessage.getThumbnailUri().startsWith("http://")
                || notifyMessage.getThumbnailUri().startsWith("https://"))) {
            valueMap.put("imageThumbnail", notifyMessage.getThumbnailUri());
        }
        if (StringUtils.isNotBlank(notifyMessage.getImageUri())
            && (notifyMessage.getImageUri().startsWith("http://")
                || notifyMessage.getImageUri().startsWith("https://"))) {
            valueMap.put("imageFullsize", notifyMessage.getImageUri());
        }
        if (StringUtils.isNotBlank(notifyMessage.getStickerPackageId())
            && StringUtils.isNumeric(notifyMessage.getStickerPackageId())) {
            valueMap.put("stickerPackageId", notifyMessage.getStickerPackageId());
        }
        if (StringUtils.isNotBlank(notifyMessage.getStickerId())
            && StringUtils.isNumeric(notifyMessage.getStickerId())) {
            valueMap.put("stickerId", notifyMessage.getStickerId());
        }
        return valueMap;
    }

}
