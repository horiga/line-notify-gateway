package org.horiga.linenotifygateway.controller;

import org.horiga.linenotifygateway.model.Notify;
import org.horiga.linenotifygateway.service.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1")
@Slf4j
public class NotifyGatewayRestController {

    private final NotifyService notifyService;

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
            NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    @RequestMapping(
            value = "/notify",
            method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<ResponseMessage> handleEvents(
            @RequestParam("service") String service,
            @RequestParam("message") String message,
            @RequestParam(name = "thumbnail_url", required = false, defaultValue = "") String thumbnailUrl,
            @RequestParam(name = "image_url", required = false, defaultValue = "") String imageUrl,
            @RequestParam(name = "sticker", required = false, defaultValue = "") String sticker
    ) throws Exception {
        notifyService.execute(new Notify(service, message, thumbnailUrl, imageUrl).addSticker(sticker));
        return new ResponseEntity<>(ResponseMessage.SUCCESS, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/notify-with-token",
            method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<ResponseMessage> handleEvents(
            @RequestParam("service") String service,
            @RequestParam("message") String message,
            @RequestParam("token") String token,
            @RequestParam(name = "thumbnail_url", required = false, defaultValue = "") String thumbnailUrl,
            @RequestParam(name = "image_url", required = false, defaultValue = "") String imageUrl,
            @RequestParam(name = "sticker", required = false, defaultValue = "") String sticker
    ) throws Exception {
        notifyService.execute(new Notify(service, message, thumbnailUrl, imageUrl).addSticker(sticker),
                              Splitter.on(",").omitEmptyStrings().trimResults().splitToList(token));
        return new ResponseEntity<>(ResponseMessage.SUCCESS, HttpStatus.OK);
    }
}
