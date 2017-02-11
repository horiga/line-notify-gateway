package org.horiga.linenotifygateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateGroupEntity {
    private String groupId;
    private String displayName;
    private String description;
}
