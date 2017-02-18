package org.horiga.linenotifygateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceEntity {
    private String serviceId;
    private String displayName;
    private String type;
    private String templateGroupId;
    private String templateMappingType;
    private String templateMappingValue;
    private String description;
}
