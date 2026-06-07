package com.creditrisk.config;

import com.creditrisk.auth.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, DatasetBootstrapProperties.class})
public class AppConfig {
}
