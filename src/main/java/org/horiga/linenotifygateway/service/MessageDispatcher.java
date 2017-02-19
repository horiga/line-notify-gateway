package org.horiga.linenotifygateway.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.horiga.linenotifygateway.entity.ServiceEntity;
import org.horiga.linenotifygateway.exception.NotifyException;
import org.horiga.linenotifygateway.model.NotifyMessage;
import org.horiga.linenotifygateway.repository.ServiceRepository;
import org.horiga.linenotifygateway.repository.TemplateRepository;
import org.horiga.linenotifygateway.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codahale.metrics.MetricRegistry;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Service
@Slf4j
public class MessageDispatcher {

    public static final String MESSAGE_TYPE_DIRECT = "direct";

    public static final String MESSAGE_TYPE_PAYLOAD = "payload";

    private final MustacheFactory mustacheFactory;

    private final NotifyMessageClient notifyClient;

    private final ServiceRepository serviceRepository;

    private final TokenRepository tokenRepository;

    private final TemplateRepository templateRepository;

    private final MetricRegistry metricRegistry;

    @Getter
    public static class Message {

        private final String messageType;
        private final String serviceId;
        private final Map<String, Object> payload;
        private final HttpServletRequest origin;
        private final Map<String, Object> parameterMaps;

        public Message(
                String serviceId,
                String messageType,
                Map<String, Object> payload,
                HttpServletRequest servletRequest) {
            this.serviceId = serviceId;
            this.messageType = messageType;
            this.payload = payload;
            origin = servletRequest;
            parameterMaps = Maps.newHashMap();
            if (!origin.getParameterMap().isEmpty()) {
                origin.getParameterMap().forEach((k, v) -> parameterMaps.put(k, origin.getParameter(k)));
            }
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

        public Map<String, Object> getParameterMap() {
            return parameterMaps;
        }
    }

    @Builder
    @Getter
    public static class Metrics {
        private String serviceId;
        private String eventId;
        private Set<String> metricNames;


        public Set<String> fullNames() {
            if (Objects.isNull(metricNames)) {
                return Sets.newHashSet();
            }
            Set<String> fullNames = Sets.newHashSet();
            metricNames.forEach(name -> fullNames.add(serviceId + '.' + eventId + '.' + name));
            return fullNames;
        }

    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public MessageDispatcher(
            NotifyMessageClient notifyClient,
            ServiceRepository serviceRepository,
            TokenRepository tokenRepository,
            TemplateRepository templateRepository,
            MetricRegistry metricRegistry) {
        this.notifyClient = notifyClient;
        this.serviceRepository = serviceRepository;
        this.tokenRepository = tokenRepository;
        this.templateRepository = templateRepository;
        this.metricRegistry = metricRegistry;
        mustacheFactory = new DefaultMustacheFactory();
    }

    public void handleMessage(Message message) throws NotifyException {
        final ServiceEntity serviceEntity = serviceRepository.findById(message.getServiceId());
        if (Objects.isNull(serviceEntity)) {
            throw new NotifyException("unknown service, sid=" + message.getServiceId());
        }
        handleMessage(serviceEntity, message);
    }

    private void handleMessage(ServiceEntity serviceEntity, Message message)
            throws NotifyException {
        final String template = parseMessageTemplate(serviceEntity, message)
                .orElseThrow(() -> new NotifyException("Non mappings template message."));

        final String messageBody = buildMessageText(serviceEntity.getServiceId()
                                                    + '-' + serviceEntity.getTemplateGroupId()
                                                    + '-' + serviceEntity.getTemplateMappingValue(),
                                                    template,
                                                    MESSAGE_TYPE_PAYLOAD
                                                            .equalsIgnoreCase(serviceEntity.getType())
                                                    && message.isPayloadMessage()
                                                    ? message.getParameterMap() : message.getPayload());

        notifyClient.send(new NotifyMessage(serviceEntity.getServiceId(),
                                            messageBody,
                                            accessToken(serviceEntity.getServiceId(), message)));
    }

    public Optional<String> parseMessageTemplate(ServiceEntity serviceEntity, Message message) {

        // TODO refactoring

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

        log.info("Search message template, sid:{}, group_id:{}, mapping_value:{}", serviceEntity.getServiceId(),
                 serviceEntity.getTemplateGroupId(), templateMappingValue);

        // TODO: template entity added to LINE sticker's meta data.

        return Optional.ofNullable(templateRepository.getTemplate(serviceEntity.getTemplateGroupId(),
                                                                  templateMappingValue));
    }

    public List<String> accessToken(final String serviceId, Message message) {
        String parameterizeAccessToken = message.getOrigin().getParameter("notify_token");
        if (StringUtils.isNotBlank(parameterizeAccessToken)) {
            return Splitter.on(",").omitEmptyStrings().trimResults().splitToList(parameterizeAccessToken);
        }
        return tokenRepository.getAccessTokenList(serviceId);
    }

    public String buildMessageText(String name, String template, Map<String, Object> scope) {
        final StringWriter writer = new StringWriter();
        final Mustache mustache = mustacheFactory.compile(new StringReader(template), name);
        mustache.execute(writer, scope);
        final String message = writer.toString();
        log.debug("Mustache template message: [name]\n{}\n----\n{}", name, template, message);
        return message;
    }

    private void surveyMetrics(Metrics metrics) {
        metrics.fullNames().forEach(name -> metricRegistry.counter("notify.counter." + name).inc());
    }
}
