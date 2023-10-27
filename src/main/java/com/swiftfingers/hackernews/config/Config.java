package com.swiftfingers.hackernews.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/*
* Use this class to Configure beans for the app
*
* */
@Configuration
public class Config {

    @Bean
    public RestTemplate restTemplate () {
        return new RestTemplate();
    }
}
