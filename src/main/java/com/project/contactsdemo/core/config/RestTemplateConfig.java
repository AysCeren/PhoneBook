package com.project.contactsdemo.core.config;

import com.project.contactsdemo.core.properties.CityServiceProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    // Without timeouts a hanging remote service keeps the request thread waiting forever;
    // enough such requests use up Tomcat's thread pool and the whole application stops responding.
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, CityServiceProperties cityServiceProperties) {
        return builder
                .setConnectTimeout(cityServiceProperties.connectTimeout())
                .setReadTimeout(cityServiceProperties.readTimeout())
                .build();
    }
}
