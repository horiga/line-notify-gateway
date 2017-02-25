package org.horiga.linenotifygateway.controller.admin;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.horiga.linenotifygateway.controller.admin.APIsController.Forms.Service;
import org.horiga.linenotifygateway.controller.admin.APIsController.Forms.Support;
import org.horiga.linenotifygateway.controller.admin.APIsController.Forms.Template;
import org.horiga.linenotifygateway.controller.admin.APIsController.Forms.Token;
import org.horiga.linenotifygateway.service.ManagementService;
import org.horiga.linenotifygateway.service.WebhookServiceDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@SuppressWarnings("unused")
@RequestMapping("/api")
@RestController
public class APIsController {

    private final ManagementService managementService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public APIsController(ManagementService managementService,
                          WebhookServiceDispatcher webhookServiceDispatcher) {
        this.managementService = managementService;
    }

    public static class Forms {

        public static class Token {
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Add {
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
                String des; // description
                @NotNull
                @Max(300)
                String owner;
            }
        }

        public static class Service {

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Add {
                @NotNull
                @Max(10)
                @Pattern(regexp = "[0-9a-zA-Z]+")
                String sid;

                @NotNull
                @Max(100)
                String dn; // displayName

                @NotNull
                @Max(10)
                String type; // 'direct', 'payload'

                @NotNull
                @Max(10)
                String tg_id; // template group identifier

                @NotNull
                @Max(30)
                String tm_type; // template mapping type - http.header, http.parameter

                @NotNull
                @Max(30)
                String tm_value; // template mapping value - `x-github-event`

                @NotNull
                @Max(300)
                String des; // description
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class TemplateMappings {
                @NotNull
                @Max(10)
                @Pattern(regexp = "[0-9a-zA-Z]+")
                String sid;

                @NotNull
                @Max(10)
                String tg_id;

                @NotNull
                @Max(30)
                String tm_type;

                @NotNull
                @Max(30)
                String tm_value;
            }

        }

        public static class Template {

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Add {
                @NotNull
                @Max(10)
                @Pattern(regexp = "[0-9a-zA-Z]+")
                String gid;

                @NotNull
                @Max(100)
                String event; // ex) push, issues, issue_comment

                @NotNull
                @Max(100)
                String des; // description

                @NotNull
                @Max(500)
                String content;
            }
        }

        public static class Support {
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Duplicate {
                @NotNull
                @Max(10)
                @Pattern(regexp = "[0-9a-zA-Z]+")
                String from_gid;

                @NotNull
                @Max(10)
                @Pattern(regexp = "[0-9a-zA-Z]+")
                String to_gid;

                @NotNull
                @Max(10)
                @Pattern(regexp = "[0-9a-zA-Z]+")
                String to_gdn; // displayName

                @NotNull
                @Max(10)
                @Pattern(regexp = "[0-9a-zA-Z]+")
                String to_gdes; // description
            }
        }
    }

    @Builder
    @Getter
    public static class AjaxResponse {
        boolean success = true;
        Object content;
    }

    // service

    @GetMapping({ "/service", "/services" })
    public ResponseEntity<AjaxResponse> getServices() {
        return responseEntity(managementService.getServices());
    }

    @GetMapping("/service/{sid}")
    public ResponseEntity<AjaxResponse> getService(
            @PathVariable(name = "sid") String serviceIdentifier
    ) {
        return responseEntity(managementService.getServiceDetails(serviceIdentifier));
    }

    @PostMapping("/service")
    public ResponseEntity<AjaxResponse> entryService(
            @Valid Service.Add f,
            BindingResult results) {
        return responseEntity(managementService.newService(f.sid, f.dn, f.type, f.tg_id, f.tm_type, f.tm_value,
                                                           f.des));
    }

    @PostMapping("/service/mapping-condition")
    public ResponseEntity<AjaxResponse> updateMappingCondition(
            @Valid Service.TemplateMappings f,
            BindingResult results) throws Exception {
        return responseEntity(managementService
                                      .updateTemplateGroupMappingCondition(f.sid,
                                                                           f.tg_id,
                                                                           f.tm_type,
                                                                           f.tm_value));
    }

    // token

    @GetMapping("/token")
    public ResponseEntity<AjaxResponse> getTokensWithQuery(
            @RequestParam("sid") String serviceIdentifier) {
        return responseEntity(managementService.getTokens(serviceIdentifier));
    }

    @GetMapping("/token/{sid}")
    public ResponseEntity<AjaxResponse> getTokens(
            @PathVariable("sid") String serviceIdentifier) {
        return responseEntity(managementService.getTokens(serviceIdentifier));
    }

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    @PostMapping("/token")
    public ResponseEntity<AjaxResponse> activateToken(
            @Valid Token.Add f,
            BindingResult results) {
        return responseEntity(
                managementService.activateToken(f.sid, f.token, f.des, f.owner));
    }

    @DeleteMapping("/token/{sid}/{id}")
    public ResponseEntity<AjaxResponse> invalidateToken(
            @PathVariable("sid") String sid, @PathVariable("id") String id) {
        managementService.invalidateToken(sid, id);
        return responseEntity(null);
    }

    @DeleteMapping("/token/{sid}")
    public ResponseEntity<AjaxResponse> invalidateToken(
            @RequestParam("sid") String serviceIdentifier) {
        managementService.invalidateAllToken(serviceIdentifier);
        return responseEntity(null);
    }

    // templates

    @GetMapping("/template-groups")
    public ResponseEntity<AjaxResponse> getTemplateGroups() {
        return responseEntity(managementService.getTemplateGroups());
    }

    @GetMapping("/template")
    public ResponseEntity<AjaxResponse> getTemplatesWithQuery(
            @RequestParam("gid") String groupIdentifier) {
        return responseEntity(managementService.getTemplates(groupIdentifier));
    }

    @GetMapping("/template/{gid}")
    public ResponseEntity<AjaxResponse> getTemplates(
            @PathVariable("gid") String groupIdentifier) {
        return responseEntity(managementService.getTemplates(groupIdentifier));
    }

    @PostMapping("/template")
    public ResponseEntity<AjaxResponse> entryTemplateMessage(
            @Valid Template.Add f,
            BindingResult results
    ) {
        return responseEntity(managementService.addTemplate(f.gid, f.event, f.des, f.content));
    }

    @PostMapping("/template/{tid}")
    public ResponseEntity<AjaxResponse> updateTemplateContent(
            @PathVariable("tid") String templateIdentifier, @RequestParam("content") String content
    ) {
        managementService.updateTemplateContent(templateIdentifier, content);
        return responseEntity(null);
    }

    // supports

    @PostMapping("/supports/duplicate-template-group")
    public ResponseEntity<AjaxResponse> duplicateTemplateGroup(
            @Valid Support.Duplicate f,
            BindingResult results) {
        managementService.duplicateFromTemplateGroup(f.from_gid, f.to_gid, f.to_gdn, f.to_gdes);
        return responseEntity(null);
    }

    private static ResponseEntity<AjaxResponse> responseEntity(Object content) {
        return new ResponseEntity<>(AjaxResponse.builder().success(true).content(content).build(),
                                    HttpStatus.OK);
    }

}
