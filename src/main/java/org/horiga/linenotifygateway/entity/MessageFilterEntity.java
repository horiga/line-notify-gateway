package org.horiga.linenotifygateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageFilterEntity {
    private String id;
    private String groupId;
    private String mappingValue;
    private String condition;
}
