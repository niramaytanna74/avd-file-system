package com.avd.filesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditorConfig {
    @Bean(name = "auditorAwareImpl")
    public org.springframework.data.domain.AuditorAware<String> auditorAwareImpl() {
        return new AuditorAwareImpl();
    }
}
