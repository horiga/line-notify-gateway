package org.horiga.linenotifygateway.config;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(MybatisAutoConfiguration.class)
@MapperScan(basePackages = "org.horiga.linenotifygateway.repository")
public class DatabaseAutoConfiguration {
}
