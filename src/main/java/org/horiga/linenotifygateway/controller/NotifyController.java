package org.horiga.linenotifygateway.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.horiga.linenotifygateway.model.Notify;
import org.horiga.linenotifygateway.service.LINENotifyClient;
import org.horiga.linenotifygateway.service.WebhookServiceDispatcher;
import org.horiga.linenotifygateway.support.MustacheMessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Deprecated
@RestController
@RequestMapping("/v1")
@Slf4j
public class NotifyController {

    private static final Collection<String> parameterNames = Collections.unmodifiableSet(
            Sets.newHashSet("notify_service", "message", "notify_token", "thumbnail_url", "image_url",
                            "sticker"));

    private final LINENotifyClient notifyService;

    private final WebhookServiceDispatcher webhookServiceDispatcher;

    private final MustacheMessageBuilder messageBuilder;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ResponseMessage {

        public static final ResponseMessage SUCCESS = new ResponseMessage(200, "");

        @JsonProperty("status")
        private int status;

        @JsonProperty("message")
        private String message;
    }

    @Autowired
    public NotifyController(
            LINENotifyClient notifyService,
            WebhookServiceDispatcher webhookServiceDispatcher,
            MustacheMessageBuilder messageBuilder) {
        this.notifyService = notifyService;
        this.webhookServiceDispatcher = webhookServiceDispatcher;
        this.messageBuilder = messageBuilder;
    }

    @RequestMapping(
            value = "/notify",
            method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<ResponseMessage> handleEvents(
            @RequestParam("notify_service") String service,
            @RequestParam("message") String message,
            @RequestParam(name = "thumbnail_url", required = false, defaultValue = "") String thumbnailUrl,
            @RequestParam(name = "image_url", required = false, defaultValue = "") String imageUrl,
            @RequestParam(name = "sticker", required = false, defaultValue = "") String sticker,
            HttpServletRequest request
    ) throws Exception {
        notifyService.send(new Notify(service,
                                      defaultMessage(service, message, request),
                                      thumbnailUrl,
                                      imageUrl,
                                      "",
                                      "",
                                      sticker));
        return new ResponseEntity<>(ResponseMessage.SUCCESS, HttpStatus.OK);
    }

    @RequestMapping(
            value = {"/notify-with-token", "/notify-direct"},
            method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<ResponseMessage> handleEvents(
            @RequestParam("notify_service") String service,
            @RequestParam("message") String message,
            @RequestParam("notify_token") String accessToken,
            @RequestParam(name = "thumbnail_url", required = false, defaultValue = "") String thumbnailUrl,
            @RequestParam(name = "image_url", required = false, defaultValue = "") String imageUrl,
            @RequestParam(name = "sticker", required = false, defaultValue = "") String sticker,
            HttpServletRequest request
    ) throws Exception {
        notifyService.send(new Notify(service,
                                      defaultMessage(service, message, request),
                                      thumbnailUrl,
                                      imageUrl,
                                      "",
                                      accessToken,
                                      sticker));
        return new ResponseEntity<>(ResponseMessage.SUCCESS, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/{service}/payload",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> handleWebhookEvents(
            @PathVariable("service") String service,
            @RequestParam(name="token_key", required = false, defaultValue = "") String tokenKey,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request
    ) {
        webhookServiceDispatcher.dispatch(service, tokenKey, payload, request);
        return new ResponseEntity<>(ResponseMessage.SUCCESS, HttpStatus.OK);
    }

    @SuppressWarnings("unused")
    private String defaultMessage(String service, String message, HttpServletRequest request) {
        final StringBuilder sb = new StringBuilder(message).append('\n');
        request.getParameterMap().entrySet()
                     .stream()
                     .filter(entry -> !parameterNames.contains(entry.getKey()))
                     .forEach(entry -> sb.append('\n')
                                                     .append(entry.getKey())
                                                     .append(": ")
                                                     .append(Joiner.on(",").join(entry.getValue())));
        Map<String, Object> scopes = Maps.newHashMap();
        scopes.put("service", service);
        scopes.put("message", sb.toString());
        return messageBuilder.build("message.mustache", scopes);
    }
}
