package org.horiga.linenotifygateway.model;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.NoArgsConstructor;

@SuppressWarnings("unused")
@NoArgsConstructor
@Getter
public class Notify {

    private String service;
    private String message;
    private String thumbnailUri;
    private String imageUri;
    private String stickerId;
    private String stickerPackageId;

    private String tokenKey;
    private List<String> accessToken;

    public Notify(@NotNull String service, @NotNull String message,
                  String thumbnailUri, String imageUri, List<String> accessToken) {
        this.service = service;
        this.message = message;
        this.thumbnailUri = thumbnailUri;
        this.imageUri = imageUri;
        this.accessToken = accessToken;
    }

    public Notify(@NotNull String service, @NotNull String message,
                  String thumbnailUri, String imageUri,
                  String tokenKey, String accessToken, String sticker) {
        this.service = service;
        this.message = message;
        this.thumbnailUri = thumbnailUri;
        this.imageUri = imageUri;
        this.tokenKey = tokenKey;
        sticker(sticker);
    }

    /**
     * @param sticker - 'stickerPackageId,stickerId' example: 123,1
     */
    public Notify sticker(String sticker) {
        if (StringUtils.isNotBlank(sticker)) {
            List<String> items = Splitter.on(",").limit(2).splitToList(sticker);
            if (2 == items.size()) {
                stickerPackageId = items.get(0);
                stickerId = items.get(1);
            }
        }
        return this;
    }

    public Notify accessToken(String accessToken) {
        if (this.accessToken == null) {
            this.accessToken = Lists.newArrayList();
        }
        this.accessToken.add(accessToken);
        return this;
    }

    public Notify accessToken(List<String> accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public Notify stickerId(String stickerId) {
        this.stickerId = stickerId;
        return this;
    }

    public Notify stickerPackageId(String stickerPackageId) {
        this.stickerPackageId = stickerPackageId;
        return this;
    }
}
