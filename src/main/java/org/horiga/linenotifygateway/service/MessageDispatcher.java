package org.horiga.linenotifygateway.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.horiga.linenotifygateway.entity.ServiceEntity;
import org.horiga.linenotifygateway.exception.NotifyException;
import org.horiga.linenotifygateway.repository.ServiceRepository;
import org.horiga.linenotifygateway.repository.TemplateRepository;
import org.horiga.linenotifygateway.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Service
@Slf4j
public class MessageDispatcher {

    public static final String MESSAGE_TYPE_DIRECT = "direct";

    public static final String MESSAGE_TYPE_PAYLOAD = "payload";

    private final ExecutorService workerThreadPool = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("dispatcher-worker-%d").build());

    private final MustacheFactory mustacheFactory;

    private final LINENotifyClient notifyClient;

    private final ServiceRepository serviceRepository;

    private final TokenRepository tokenRepository;

    private final TemplateRepository templateRepository;

    @Getter
    public static class Message {

        private final String messageType;
        private final String serviceId;
        private final Map<String, Object> payload;
        private final HttpServletRequest origin;

        public Message(
                String serviceId,
                String messageType,
                Map<String, Object> payload,
                HttpServletRequest servletRequest) {
            this.serviceId = serviceId;
            this.messageType = messageType;
            this.payload = payload;
            origin = servletRequest;
        }

        public boolean isPayloadMessage() {
            return MESSAGE_TYPE_PAYLOAD.equals(messageType)
                   && !Objects.isNull(payload);
        }

        public String getThumbnailUrl() {
            return origin.getParameter("thumbnail_url");
        }

        public String getImageUrl() {
            return origin.getParameter("image_url");
        }

        public String getStickerPackageId() {
            return origin.getParameter("sticker_package_id");
        }

        public String getStickerId() {
            return origin.getParameter("sticker_id");
        }

        public String getHeader(String name) {
            return getHeader(name, "");
        }

        public String getHeader(String name, String defaultValue) {
            return StringUtils.defaultString(origin.getHeader(name), defaultValue);
        }

        public String getParameter(String name) {
            return getParameter(name, "");
        }

        public String getParameter(String name, String defaultValue) {
            return StringUtils.defaultString(origin.getParameter(name), defaultValue);
        }
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public MessageDispatcher(
            LINENotifyClient notifyClient,
            ServiceRepository serviceRepository,
            TokenRepository tokenRepository,
            TemplateRepository templateRepository) {
        this.notifyClient = notifyClient;
        this.serviceRepository = serviceRepository;
        this.tokenRepository = tokenRepository;
        this.templateRepository = templateRepository;
        mustacheFactory = new DefaultMustacheFactory();
    }

    public void handleMessage(Message message) throws NotifyException {

        final ServiceEntity serviceEntity = serviceRepository.findById(message.getServiceId());
        if (Objects.isNull(serviceEntity)) {
            throw new NotifyException("unknown service, sid=" + message.getServiceId());
        }

        // サービスからテンプレートの 'mapping_type', 'mapping_value' を判断してテンプレートを検索

        // 送信するメッセージを構築

        workerThreadPool.execute(() -> {
            try {

            } catch (Throwable ignore) {
                //
            }
        });

        surveyMetrics(message);
    }

    public Optional<String> getMessageTemplate(ServiceEntity serviceEntity, Message message) {
        String templateMappingValue = "none";
        if (message.isPayloadMessage()) {
            // payload
            if ("http.header".equalsIgnoreCase(serviceEntity.getTemplateMappingType())) {
                templateMappingValue = message.getHeader(serviceEntity.getTemplateMappingValue(), "");
            }
            if ("parameter".equalsIgnoreCase(serviceEntity.getTemplateMappingType())) {
                templateMappingValue = message.getPayload().containsKey(
                        serviceEntity.getTemplateMappingType()) ?
                                       (String) message.getPayload().get(
                                               serviceEntity.getTemplateMappingValue())
                                                                : message.getParameter(
                        serviceEntity.getTemplateMappingValue(), "");
            }
        } else {
            if ("parameter".equalsIgnoreCase(serviceEntity.getTemplateMappingType())) {
                templateMappingValue = message.getParameter(serviceEntity.getTemplateMappingValue(), "");
            }
        }
        log.info("Search message template, sid:{}, group_id:{}, mapping_value:{}", serviceEntity.getService(),
                 serviceEntity.getTemplateGroupId(), templateMappingValue);
        return Optional.ofNullable(templateRepository.getTemplate(serviceEntity.getTemplateGroupId(),
                                                                  templateMappingValue));
    }

    public String buildMessageWithTemplate(String name, String template, Map<String, Object> scope) {
        final StringWriter writer = new StringWriter();
        final Mustache mustache = mustacheFactory.compile(new StringReader(template), name);
        mustache.execute(writer, scope);
        final String message = writer.toString();
        log.debug("Mustache template message: [name]\n{}\n----\n{}", name, template, message);
        return message;
    }

    private void surveyMetrics(Message message) {
    }
}
