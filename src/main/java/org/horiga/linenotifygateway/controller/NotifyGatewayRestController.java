package org.horiga.linenotifygateway.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.horiga.linenotifygateway.model.Notify;
import org.horiga.linenotifygateway.service.NotifyService;
import org.horiga.linenotifygateway.service.WebhookServiceDispatcher;
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
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1")
@Slf4j
public class NotifyGatewayRestController {

    private static final Collection<String> parameterNames = Collections.unmodifiableSet(
            Sets.newHashSet("notify_service", "message", "notify_token", "thumbnail_url", "image_url",
                            "sticker"));

    private final NotifyService notifyService;

    private final WebhookServiceDispatcher webhookServiceDispatcher;

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
    public NotifyGatewayRestController(
            NotifyService notifyService,
            WebhookServiceDispatcher webhookServiceDispatcher) {
        this.notifyService = notifyService;
        this.webhookServiceDispatcher = webhookServiceDispatcher;
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
        notifyService.execute(
                new Notify(service, buildMessage(service, message, request), thumbnailUrl, imageUrl)
                        .addSticker(sticker));
        return new ResponseEntity<>(ResponseMessage.SUCCESS, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/notify-with-token",
            method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<ResponseMessage> handleEvents(
            @RequestParam("notify_service") String service,
            @RequestParam("message") String message,
            @RequestParam("notify_token") String token,
            @RequestParam(name = "thumbnail_url", required = false, defaultValue = "") String thumbnailUrl,
            @RequestParam(name = "image_url", required = false, defaultValue = "") String imageUrl,
            @RequestParam(name = "sticker", required = false, defaultValue = "") String sticker,
            HttpServletRequest request
    ) throws Exception {
        notifyService.execute(
                new Notify(service, buildMessage(service, message, request), thumbnailUrl, imageUrl)
                        .addSticker(sticker),
                Splitter.on(",").omitEmptyStrings().trimResults().splitToList(token));
        return new ResponseEntity<>(ResponseMessage.SUCCESS, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/{service}/payload",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> handleWebhookEvents(
            @PathVariable("service") String service,
            @RequestBody Map<String, Object> message,
            HttpServletRequest request
    ) {
        webhookServiceDispatcher.dispatch(service, message, request);
        return new ResponseEntity<>(ResponseMessage.SUCCESS, HttpStatus.OK);
    }

    @SuppressWarnings("unused")
    private static String buildMessage(String service, String message, HttpServletRequest request) {
        final StringBuilder messageBuilder = new StringBuilder(message).append('\n');
        request.getParameterMap().entrySet()
                     .stream()
                     .filter(entry -> !parameterNames.contains(entry.getKey()))
                     .forEach(entry -> messageBuilder.append('\n')
                                                     .append(entry.getKey())
                                                     .append(": ")
                                                     .append(Joiner.on(",").join(entry.getValue())));
        return messageBuilder.toString();
    }


}
