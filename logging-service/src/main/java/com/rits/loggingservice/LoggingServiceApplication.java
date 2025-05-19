package com.rits.loggingservice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class LoggingServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(LoggingServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(LoggingServiceApplication.class, args);
    }

    // Log some messages on startup for demonstration purposes.
    @PostConstruct
    public void init() {
        logger.info("Logging Service Application Started");
        logger.debug("Debug message example");
        logger.warn("Warning message example");
        logger.error("Error message example");
    }
}
