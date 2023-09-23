package ru.dankoy.korvotoanki.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AppProperties.class, GoogleParamsProperties.class})
public class AppPropertiesConfig {

}
