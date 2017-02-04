package org.horiga.linenotifygateway.controller.admin;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.horiga.linenotifygateway.controller.admin.APIsController.Forms.ServiceForm;
import org.horiga.linenotifygateway.controller.admin.APIsController.Forms.TokenForm;
import org.horiga.linenotifygateway.entity.ServiceEntity;
import org.horiga.linenotifygateway.entity.TokenEntity;
import org.horiga.linenotifygateway.repository.ServiceRepository;
import org.horiga.linenotifygateway.repository.TokenRepository;
import org.horiga.linenotifygateway.service.WebhookServiceDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@RequestMapping("/api")
@RestController
public class APIsController {

    private final TokenRepository tokenRepository;

    private final ServiceRepository serviceRepository;

    private final WebhookServiceDispatcher webhookServiceDispatcher;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public APIsController(TokenRepository tokenRepository,
                          ServiceRepository serviceRepository,
                          WebhookServiceDispatcher webhookServiceDispatcher) {
        this.tokenRepository = tokenRepository;
        this.serviceRepository = serviceRepository;
        this.webhookServiceDispatcher = webhookServiceDispatcher;
    }

    public static class Forms {
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TokenForm {
            @NotNull
            @Max(10)
            @Pattern(regexp="[0-9a-zA-Z]+")
            String sid;
            @NotNull
            @Max(33)
            @Pattern(regexp="[0-9a-zA-Z]+")
            String token;
            @NotNull
            @Max(300)
            String descr;
            @NotNull
            @Max(300)
            String owner;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ServiceForm {
            @NotNull
            @Max(10)
            @Pattern(regexp="[0-9a-zA-Z]+")
            String sid;

            @NotNull
            @Max(10)
            String type;

            @NotNull
            @Max(10)
            String tgid;

            @NotNull
            @Max(300)
            String descr;
        }
    }

    @Builder
    @Getter
    public static class AjaxResponse {
        boolean success = true;
        Object content;
    }

    @GetMapping("/dispatchers")
    public ResponseEntity<AjaxResponse> getWebhookDispatcher() {
        return responseEntity(webhookServiceDispatcher.getAvailableDispatcher());
    }

    @GetMapping({"/", ""})
    public ResponseEntity<AjaxResponse> getData() {
        final Map<String, Object> content = Maps.newTreeMap();
        content.put("service", serviceRepository.findAll());
        content.put("token", tokenRepository.findAll().stream()
                       .collect(Collectors.groupingBy(TokenEntity::getService)));
        return responseEntity(content);
    }

    @GetMapping("/service")
    public ResponseEntity<AjaxResponse> getServices() {
        return responseEntity(serviceRepository.findAll());
    }

    @PostMapping("/service")
    public ResponseEntity<AjaxResponse> addService(
            @Valid ServiceForm fm, BindingResult results) {
        final ServiceEntity entity = new ServiceEntity(fm.sid, fm.type, fm.tgid, fm.descr);
        serviceRepository.insert(entity);
        return responseEntity(entity);
    }

    @GetMapping("/token")
    public ResponseEntity<AjaxResponse> getTokens(@RequestParam("sid") String sid) {
        return responseEntity(tokenRepository.findByServiceId(sid));
    }

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    @PostMapping("/token")
    public ResponseEntity<AjaxResponse> addToken(
            @Valid TokenForm fm, BindingResult results) {
        final TokenEntity token = new TokenEntity(UUID.randomUUID().toString().replaceAll("-", ""),
                                            fm.sid, fm.token, fm.descr, fm.owner);
        tokenRepository.insert(token);
        return responseEntity(token);
    }

    @DeleteMapping("/token")
    public ResponseEntity<AjaxResponse> deleteToken(@RequestParam("id") String id) {
        tokenRepository.delete(id);
        return responseEntity(null);
    }

    @DeleteMapping("/all-token")
    public ResponseEntity<AjaxResponse> deleteAllToken(@RequestParam("sid") String sid) {
        tokenRepository.deleteByServiceId(sid);
        return responseEntity(null);
    }

    private static ResponseEntity<AjaxResponse> responseEntity(Object content) {
        return new ResponseEntity<>(AjaxResponse.builder().success(true).content(content).build(),
                                    HttpStatus.OK);
    }
}
