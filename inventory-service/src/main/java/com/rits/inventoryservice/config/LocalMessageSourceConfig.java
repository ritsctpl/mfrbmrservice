package com.rits.inventoryservice.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class LocalMessageSourceConfig {
    @Bean
    public MessageSource localMessageSource(){
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("local");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
