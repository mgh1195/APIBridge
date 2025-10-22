package com.wrapper.apibridge.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.podium")
@Getter
@Setter
public class PodiumAPIConfiguration {
    private String url;
    private String token;
    private String tokenIssuer;
}
