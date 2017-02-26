package org.horiga.linenotifygateway.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.horiga.linenotifygateway.entity.MessageFilterEntity;
import org.horiga.linenotifygateway.entity.ServiceEntity;
import org.horiga.linenotifygateway.entity.TemplateEntity;
import org.horiga.linenotifygateway.exception.MessageFilterException;
import org.horiga.linenotifygateway.exception.NotifyException;
import org.horiga.linenotifygateway.model.NotifyMessage;
import org.horiga.linenotifygateway.repository.MessageFilterRepository;
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
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

    public static final Set<String> SIMPLE_METRICS_NAME = ImmutableSet.of("event");

    private final MustacheFactory mustacheFactory;

    private final NotifyMessageClient notifyClient;

    private final ServiceRepository serviceRepository;

    private final TokenRepository tokenRepository;

    private final TemplateRepository templateRepository;

    private final MessageFilterRepository messageFilterRepository;

    private final MetricRegistry metricRegistry;

    public enum MappingType {

        HTTP_HEADER("http.header"),
        HTTP_REQUEST_PARAMETER("parameter"),
        NONE("none");

        MappingType(String value) {
            this.value = value;
        }

        @Getter
        private final String value;

        public static MappingType mappingType(String val) {
            return Arrays.stream(values())
                         .filter(v -> v.value.equalsIgnoreCase(val))
                         .findFirst()
                         .orElse(NONE);
        }
    }

    @Getter
    public static class Message {

        private final String messageType;
        private final String serviceId;
        private final Map<String, Object> payload;
        private final HttpServletRequest origin;
        private final Map<String, Object> parameterMaps;
        private final List<String> accessToken;
        private final boolean jsonPayload;

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
            final String parameterizeAccessToken = origin.getParameter("notify_token");
            accessToken = StringUtils.isNotBlank(parameterizeAccessToken)
                          ? Splitter.on(",").omitEmptyStrings().trimResults()
                                    .splitToList(parameterizeAccessToken) : Lists.newArrayList();
            jsonPayload = !Objects.isNull(payload)
                          && StringUtils.defaultString(origin.getHeader("Content-Type"), "")
                                        .toLowerCase().contains("json");
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
            metricNames.forEach(name -> fullNames.add(name + '.' + serviceId + '.' + eventId));
            return fullNames;
        }

    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public MessageDispatcher(
            @NotNull NotifyMessageClient notifyClient,
            @NotNull ServiceRepository serviceRepository,
            @NotNull TokenRepository tokenRepository,
            @NotNull TemplateRepository templateRepository,
            @NotNull MessageFilterRepository messageFilterRepository,
            @NotNull MetricRegistry metricRegistry) {
        this.notifyClient = notifyClient;
        this.serviceRepository = serviceRepository;
        this.tokenRepository = tokenRepository;
        this.templateRepository = templateRepository;
        this.messageFilterRepository = messageFilterRepository;
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

    private void handleMessage(ServiceEntity serviceEntity, Message message) throws NotifyException {
        try {

            final Optional<TemplateEntity> templateEntityOpt = parseMessageTemplate(serviceEntity, message);

            if (!templateEntityOpt.isPresent()) {
                log.info("This event is not mapping a template. {}, {}", serviceEntity, message);
                return;
            }

            final TemplateEntity templateEntity = templateEntityOpt.get();

            surveyMetrics(Metrics.builder()
                                 .metricNames(SIMPLE_METRICS_NAME)
                                 .serviceId(serviceEntity.getServiceId())
                                 .eventId(templateEntity.getMappingValue()).build());

            notifyClient.send(new NotifyMessage(serviceEntity.getServiceId(),
                                                buildMessageText(templateName(serviceEntity),
                                                                 templateEntity.getContent(),
                                                                 MESSAGE_TYPE_PAYLOAD.equalsIgnoreCase(
                                                                         serviceEntity.getType())
                                                                 && message.isPayloadMessage()
                                                                 ? message.getParameterMap() :
                                                                 message.getPayload()),
                                                message.getAccessToken().isEmpty() ?
                                                tokenRepository.getAccessTokenList(serviceEntity.getServiceId())
                                                                                   : message.getAccessToken())
                                      .sticker(templateEntity.getSticker()));

        } catch (MessageFilterException filtered) {
            log.warn("This event was filtered, ", filtered);
        }
    }

    public Optional<TemplateEntity> parseMessageTemplate(ServiceEntity serviceEntity, Message message)
            throws NotifyException {

        String mappingValue = "none";
        if (message.isPayloadMessage()) {
            if (MappingType.HTTP_HEADER.getValue().equalsIgnoreCase(
                    serviceEntity.getTemplateMappingType())) {
                mappingValue = message.getHeader(serviceEntity.getTemplateMappingValue(), "");
            }

            if (MappingType.HTTP_REQUEST_PARAMETER.getValue().equalsIgnoreCase(
                    serviceEntity.getTemplateMappingType())) {
                mappingValue = validateMappingValueWithMessageRequest(serviceEntity.getTemplateMappingType(),
                                                                      serviceEntity.getTemplateMappingValue(),
                                                                      message);
            }
        } else {
            if (MappingType.HTTP_REQUEST_PARAMETER.getValue().equalsIgnoreCase(
                    serviceEntity.getTemplateMappingType())) {
                mappingValue = message.getParameter(serviceEntity.getTemplateMappingValue(), "");
            }
        }

        Optional<TemplateEntity> templateEntityOpt = Optional.ofNullable(templateRepository.getTemplateEntity(
                serviceEntity.getTemplateGroupId(), mappingValue));

        log.info("Search message template, service_id:{}, group_id:{}, mapping_value:{}",
                 serviceEntity.getServiceId(), serviceEntity.getTemplateGroupId(), mappingValue);

        filter(message, templateEntityOpt.orElse(null));

        return templateEntityOpt;
    }

    public String validateMappingValueWithMessageRequest(String mappingType, String mappingValue,
                                                         Message message) {
        if (message.getParameterMap().containsKey(mappingValue)) {
            return message.getParameter(mappingValue, "");
        }
        if (message.getPayload().containsKey(mappingValue)) {
            return StringUtils.defaultString((String) message.getPayload().get(mappingValue), "");
        }
        return "";
    }

    private void filter(Message message, TemplateEntity templateEntity) throws MessageFilterException {
        if (!Objects.isNull(templateEntity)) {
            List<MessageFilterEntity> filters =
                    messageFilterRepository.findByMappings(templateEntity.getGroupId(),
                                                           templateEntity.getMappingValue());
            if (!filters.isEmpty()) {
                // TODO jayway/JsonPath
            }
        }
    }

    private static String templateName(ServiceEntity serviceEntity) {
        return serviceEntity.getServiceId() + '-' + serviceEntity.getTemplateGroupId() + '-' + serviceEntity
                .getTemplateMappingValue();
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
        if (Objects.isNull(metrics)) {
            return;
        }
        metrics.fullNames().forEach(name -> metricRegistry.counter("notify.counter." + name).inc());
    }
}
