package com.casbytes.core.configuration;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer casbytesJsonMapperCustomizer() {
        return (JsonMapper.Builder builder) -> builder.findAndAddModules();
    }
}
