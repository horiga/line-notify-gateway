package org.horiga.linenotifygateway.support;

import java.io.StringWriter;
import java.nio.file.FileSystems;

import org.horiga.linenotifygateway.config.LineNotifyGatewayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MustacheMessageBuilder {

    private final MustacheFactory mustacheFactory;

    @Autowired
    public MustacheMessageBuilder(LineNotifyGatewayProperties properties) {
        final String templateRootPath = properties.getMustacheTemplatePath();
        mustacheFactory = templateRootPath.startsWith("/")
                          ? new DefaultMustacheFactory(
                FileSystems.getDefault().getPath(templateRootPath).toFile())
                          : new DefaultMustacheFactory(templateRootPath);
    }

    public <T> String build(String template, T scopes) {
        Mustache mustache = mustacheFactory.compile(template);
        final StringWriter writer = new StringWriter();
        mustache.execute(writer, scopes);
        final String message = writer.toString();
        log.debug("Compile mustache template message: [{}] \n {}", template, message);
        return message;
    }
}
