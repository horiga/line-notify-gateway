package org.horiga.linenotifygateway.service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Service
@Slf4j
public class MessageDispatcherService {

    public static final String MESSAGE_TYPE_DIRECT = "direct";

    public static final String MESSAGE_TYPE_PAYLOAD = "payload";

    private final ExecutorService workerThreadPool = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("dispatcher-worker-%d").build());

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
            origin.getParameter("sticker_package_id");
            return null;
        }

        public String getStickerId() {
            origin.getParameter("sticker_id");
            return null;
        }

        public String getHeader(String name, String defaultValue) {
            return StringUtils.defaultString(origin.getHeader(name), defaultValue);
        }
    }

    @Autowired
    public MessageDispatcherService() {
    }

    public void handleMessage(Message message) {

        // サービスからテンプレートグループを取得

        // サービスからテンプレートの 'mapping_type', 'mapping_value' を判断してテンプレートを検索

        // 送信するメッセージを構築

        // ワーカスレッドで送信
        workerThreadPool.execute(() -> {
            try {
                //
            } catch (Throwable ignore) {
                //
            }
        });

        surveyMetrics(message);
    }

    private void surveyMetrics(Message message) {
    }
}
