package org.horiga.linenotifygateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenEntity {
    private String id;
    private String service;
    private String token;
    private String description;
    private String owner;
}
