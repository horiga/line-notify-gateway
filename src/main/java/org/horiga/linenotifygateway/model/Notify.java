package org.horiga.linenotifygateway.model;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.base.Splitter;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class Notify {

    private String service;
    private String message;
    private String thumbnailUri;
    private String imageUri;
    private String stickerId;
    private String stickerPackageId;

    public Notify(@NotNull String service, @NotNull String message,
                  String thumbnailUri, String imageUri) {
        this.service = service;
        this.message = message;
        this.thumbnailUri = thumbnailUri;
        this.imageUri = imageUri;
    }

    /**
     * @param sticker - 'stickerPackageId,stickerId' example: 123,1
     */
    public Notify addSticker(String sticker) {
        if (StringUtils.isNotBlank(sticker)) {
            List<String> items = Splitter.on(",").limit(2).splitToList(sticker);
            if (2 == items.size()) {
                stickerPackageId = items.get(0);
                stickerId = items.get(1);
            }
        }
        return this;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public void setStickerPackageId(String stickerPackageId) {
        this.stickerPackageId = stickerPackageId;
    }

    public MultiValueMap<String, String> valueMap() {
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        String _message = new StringBuilder()
                .append("Message from '").append(service).append("' service\n\n")
                .append(message)
                .toString();
        if (_message.length() >= 1000) {
            _message = _message.substring(0, 950) + " ...";
        }
        valueMap.add("message", _message);
        if (StringUtils.isNotBlank(thumbnailUri) && (thumbnailUri.startsWith("http://") || thumbnailUri
                .startsWith("https://"))) {
            valueMap.add("imageThumbnail", thumbnailUri);
        }
        if (StringUtils.isNotBlank(imageUri) && (imageUri.startsWith("http://") || imageUri
                .startsWith("https://"))) {
            valueMap.add("imageFullsize", imageUri);
        }
        if (StringUtils.isNotBlank(stickerPackageId) && StringUtils.isNumeric(stickerPackageId)) {
            valueMap.add("stickerPackageId", stickerPackageId);
        }
        if (StringUtils.isNotBlank(stickerId) && StringUtils.isNumeric(stickerId)) {
            valueMap.add("stickerId", stickerId);
        }
        return valueMap;
    }
}
