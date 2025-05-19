package com.rits;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PcoIntegrationServiceApplication {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    public static void main(String[] args) {
        SpringApplication.run(PcoIntegrationServiceApplication.class, args);
    }

}
