package com.rits.loggingservice.config;


import ch.qos.logback.classic.LoggerContext;
import com.rits.loggingservice.appender.WebSocketLogAppender;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ch.qos.logback.classic.Logger;

@Configuration
public class LogbackInjectorConfig {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostConstruct
    public void init() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger("ROOT");
        // Retrieve the custom appender by name (as defined in logback-spring.xml)
        WebSocketLogAppender wsAppender = (WebSocketLogAppender) rootLogger.getAppender("WEB_SOCKET");
        if (wsAppender != null) {
            wsAppender.setMessagingTemplate(messagingTemplate);
        }
    }
}
