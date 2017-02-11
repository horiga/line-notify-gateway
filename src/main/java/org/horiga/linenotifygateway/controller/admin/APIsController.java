package org.horiga.linenotifygateway.controller.admin;

import java.util.Map;
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
import org.horiga.linenotifygateway.service.ManagementService;
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

    private final ManagementService managementService;

    private final TokenRepository tokenRepository;

    private final ServiceRepository serviceRepository;

    private final WebhookServiceDispatcher webhookServiceDispatcher;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public APIsController(ManagementService managementService, TokenRepository tokenRepository,
                          ServiceRepository serviceRepository,
                          WebhookServiceDispatcher webhookServiceDispatcher) {
        this.managementService = managementService;
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
            @Pattern(regexp = "[0-9a-zA-Z]+")
            String sid;
            @NotNull
            @Max(33)
            @Pattern(regexp = "[0-9a-zA-Z]+")
            String token;
            @NotNull
            @Max(300)
            String description;
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
            @Pattern(regexp = "[0-9a-zA-Z]+")
            String sid;

            @NotNull
            @Max(100)
            String dn;

            @NotNull
            @Max(10)
            String type; // 'direct', 'payload'

            @NotNull
            @Max(10)
            String tg_id;

            @NotNull
            @Max(30)
            String tm_type; // http.header, http.parameter

            @NotNull
            @Max(30)
            String tm_value; // `x-github-event`

            @NotNull
            @Max(300)
            String description;
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

    @GetMapping({ "/", "" })
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
    public ResponseEntity<AjaxResponse> newService(
            @RequestParam(name="from", required = false, defaultValue = "") String from, // 'github' or 'alert'
            @Valid ServiceForm form,
            BindingResult results) {
        final ServiceEntity entity =
                new ServiceEntity(form.sid, form.dn, form.type, form.tg_id, form.tm_type, form.tm_value,
                                  form.description);
        serviceRepository.insert(entity);
        return responseEntity(entity);
    }

    @GetMapping("/token")
    public ResponseEntity<AjaxResponse> getTokens(@RequestParam("sid") String sid) {
        return responseEntity(tokenRepository.findByServiceId(sid));
    }

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    @PostMapping("/token")
    public ResponseEntity<AjaxResponse> activateToken(
            @Valid TokenForm form, BindingResult results) {
        return responseEntity(
                managementService.activateToken(form.sid, form.token, form.description, form.owner));
    }

    @DeleteMapping("/token")
    public ResponseEntity<AjaxResponse> invalidateToken(
            @RequestParam("sid") String sid, @RequestParam("id") String id) {
        managementService.invalidateToken(sid, id);
        return responseEntity(null);
    }

    @DeleteMapping("/all-token")
    public ResponseEntity<AjaxResponse> invalidateToken(@RequestParam("sid") String sid) {
        managementService.invalidateAllToken(sid);
        return responseEntity(null);
    }

    private static ResponseEntity<AjaxResponse> responseEntity(Object content) {
        return new ResponseEntity<>(AjaxResponse.builder().success(true).content(content).build(),
                                    HttpStatus.OK);
    }
}
