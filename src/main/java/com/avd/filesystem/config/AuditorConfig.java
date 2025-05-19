package com.avd.filesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class AuditorConfig {
    @Bean(name = "auditorAwareImpl")
    public org.springframework.data.domain.AuditorAware<String> auditorAwareImpl() {
        return new AuditorAwareImpl();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
