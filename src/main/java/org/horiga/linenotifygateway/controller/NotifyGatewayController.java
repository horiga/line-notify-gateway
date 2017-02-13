package org.horiga.linenotifygateway.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.horiga.linenotifygateway.controller.NotifyController.ResponseMessage;
import org.horiga.linenotifygateway.service.MessageDispatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class NotifyGatewayController {

    private final MessageDispatcher dispatcher;

    public NotifyGatewayController(MessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @RequestMapping(
            value = "/v2/{sid}/direct",
            method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<ResponseMessage> direct(
            @PathVariable("sid") String sid,
            @RequestParam(name = "m") String message,
            @RequestParam(name = "tag", required = false, defaultValue = "") String tag,
            @RequestParam(name = "thumbnail_url", required = false, defaultValue = "") String thumbnailUrl,
            @RequestParam(name = "image_url", required = false, defaultValue = "") String imageUrl,
            @RequestParam(name = "sticker", required = false, defaultValue = "") String sticker,
            HttpServletRequest servletRequest
    ) {
        dispatcher.handleMessage(null);
        return new ResponseEntity<>(ResponseMessage.SUCCESS, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/v2/{sid}/payload",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> payload(
            @PathVariable("sid") String sid,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest servletRequest
    ) {
        dispatcher.handleMessage(null);
        return new ResponseEntity<>(ResponseMessage.SUCCESS, HttpStatus.OK);
    }
}
