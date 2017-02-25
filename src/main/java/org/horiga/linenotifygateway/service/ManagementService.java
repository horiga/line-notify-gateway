package org.horiga.linenotifygateway.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.horiga.linenotifygateway.entity.ServiceEntity;
import org.horiga.linenotifygateway.entity.TemplateEntity;
import org.horiga.linenotifygateway.entity.TemplateGroupEntity;
import org.horiga.linenotifygateway.entity.TokenEntity;
import org.horiga.linenotifygateway.exception.NotifyException;
import org.horiga.linenotifygateway.repository.ServiceRepository;
import org.horiga.linenotifygateway.repository.TemplateRepository;
import org.horiga.linenotifygateway.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Service
@Slf4j
public class ManagementService {

    private static final String ID_PREFIX_MESSAGE_TEMPLATE = "m";
    private static final String ID_PREFIX_TOKEN = "t";

    private final ServiceRepository serviceRepository;
    private final TokenRepository tokenRepository;
    private final TemplateRepository templateRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ManagementService(ServiceRepository serviceRepository,
                             TokenRepository tokenRepository,
                             TemplateRepository templateRepository) {
        this.serviceRepository = serviceRepository;
        this.tokenRepository = tokenRepository;
        this.templateRepository = templateRepository;
    }

    @Builder
    @Getter
    public static class ServiceDetail {
        ServiceEntity service;
        List<TokenEntity> tokens;
        List<TemplateEntity> templates;
    }

    public List<ServiceEntity> getServices() {
        return serviceRepository.findAll();
    }

    public ServiceEntity newService(@NotNull String serviceIdentifier,
                                    @NotNull String displayName,
                                    @NotNull String type,
                                    @NotNull String templateGroupIdentifier,
                                    @NotNull String templateMappingType,
                                    @NotNull String templateMappingValue,
                                    @NotNull String description) {
        final ServiceEntity entry =
                new ServiceEntity(serviceIdentifier, displayName, type, templateGroupIdentifier,
                                  templateMappingType, templateMappingValue, description);
        serviceRepository.insert(entry);
        return entry;
    }

    public ServiceEntity updateTemplateGroupMappingCondition(@NotNull String serviceIdentifier,
                                                             @NotNull String templateGroupIdentifier,
                                                             String templateMappingType,
                                                             String templateMappingValue)
            throws Exception {
        ServiceEntity changes = serviceRepository.findById(serviceIdentifier);
        if (Objects.isNull(changes)) {
            throw new NotifyException("Error, Not Found target service entity. (" + serviceIdentifier + ')');
        }
        changes.setTemplateGroupId(templateGroupIdentifier);
        changes.setTemplateMappingType(StringUtils.defaultString(templateMappingType, "none"));
        changes.setTemplateMappingValue(StringUtils.defaultString(templateMappingValue, ""));
        serviceRepository.updateTemplateGroupConditions(changes);
        return changes;
    }

    public ServiceDetail getServiceDetails(@NotNull String serviceIdentifier) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceIdentifier);
        return ServiceDetail.builder()
                            .service(serviceEntity)
                            .tokens(tokenRepository.findByServiceId(serviceIdentifier))
                            .templates(
                                    templateRepository.findTemplateByGroup(serviceEntity.getTemplateGroupId()))
                            .build();
    }

    public TokenEntity activateToken(@NotNull String serviceIdentifier,
                                     @NotNull String token,
                                     String description,
                                     @NotNull String owner) {
        final TokenEntity newEntity = new TokenEntity(newIdentify(ID_PREFIX_TOKEN), serviceIdentifier, token,
                                                      StringUtils.defaultString(description, ""), owner);
        tokenRepository.insert(newEntity);
        return newEntity;
    }

    public void invalidateToken(@NotNull String serviceIdentifier,
                                @NotNull String tokenIdentifier) {
        tokenRepository.delete(tokenIdentifier, serviceIdentifier);
    }

    public void invalidateAllToken(@NotNull String serviceIdentifier) {
        tokenRepository.deleteWithServiceId(serviceIdentifier);
    }

    public List<TokenEntity> getTokens(@NotNull String serviceIdentifier) {
        return tokenRepository.findByServiceId(serviceIdentifier);
    }

    public List<TemplateGroupEntity> getTemplateGroups() {
        return templateRepository.findGroups();
    }

    public List<TemplateEntity> getTemplates(String groupIdentifier) {
        return templateRepository.findTemplateByGroup(groupIdentifier);
    }

    public void duplicateFromTemplateGroup(@NotNull String fromTemplateGroupIdentifier,
                                           @NotNull String toTemplateGroupIdentifier,
                                           @NotNull String toTemplateGroupDisplayName,
                                           @NotNull String toTemplateGroupDescription) {
        // new template group.
        templateRepository.addTemplateGroup(new TemplateGroupEntity(toTemplateGroupIdentifier,
                                                                    toTemplateGroupDisplayName,
                                                                    toTemplateGroupDescription));
        // duplicate templates from specified other template group identifier.
        templateRepository.findTemplateByGroup(
                fromTemplateGroupIdentifier).forEach(t -> templateRepository
                .addTemplate(
                        new TemplateEntity(newIdentify(ID_PREFIX_MESSAGE_TEMPLATE), toTemplateGroupIdentifier,
                                           t.getMappingValue(),
                                           StringUtils.defaultString(t.getDescription(), ""),
                                           t.getSticker(),
                                           t.getContent())));
    }

    public TemplateEntity addTemplate(String groupIdentifier, String mappingValue, String description,
                                      String sticker, String content) {
        final TemplateEntity newEntity = new TemplateEntity(newIdentify(ID_PREFIX_MESSAGE_TEMPLATE),
                                                            groupIdentifier,
                                                            StringUtils.defaultString(mappingValue, "none"),
                                                            StringUtils.defaultString(description, ""),
                                                            StringUtils.defaultString(sticker, ""),
                                                            content);
        templateRepository.addTemplate(newEntity);
        return newEntity;
    }

    public void updateTemplateContent(@NotNull String templateIdentifier,
                                      @NotNull String content) {
        templateRepository.updateTemplate(templateIdentifier, content);
    }

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    protected static String newIdentify(String prefix) {
        return prefix + UUID.randomUUID().toString().replaceAll("-", "");
    }
}
