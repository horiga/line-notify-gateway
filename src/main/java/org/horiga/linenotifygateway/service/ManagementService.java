package org.horiga.linenotifygateway.service;

import java.time.Instant;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.horiga.linenotifygateway.entity.ServiceEntity;
import org.horiga.linenotifygateway.entity.TemplateEntity;
import org.horiga.linenotifygateway.entity.TokenEntity;
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

    public ServiceDetail getServiceDetails(@NotNull String serviceId) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId);
        return ServiceDetail.builder()
                            .service(serviceEntity)
                            .tokens(null)
                            .templates(null)
                            .build();
    }

    public void newServiceWith(String serviceId) {
    }

    public void newService(
    ) {
    }

    public TokenEntity activateToken(@NotNull String serviceId,
                              @NotNull String token,
                              String description,
                              @NotNull String owner) {
        final String _id = Long.toString(Instant.now().getEpochSecond());
        final TokenEntity newToken = new TokenEntity(_id, serviceId, token, StringUtils.defaultString(description, ""), owner);
        tokenRepository.insert(newToken);
        return newToken;
    }

    public void invalidateToken(@NotNull String serviceId,
                                @NotNull String tokenId) {
        tokenRepository.delete(tokenId, serviceId);
    }

    public void invalidateAllToken(@NotNull String serviceId) {
        tokenRepository.deleteWithServiceId(serviceId);
    }

    public void updateTemplate(@NotNull String id,
                               @NotNull String description,
                               @NotNull String content) {
        templateRepository.updateTemplate(id, description, content);
    }
}
