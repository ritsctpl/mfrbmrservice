package com.rits.loggingservice.appender;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class WebSocketLogAppender extends AppenderBase<ILoggingEvent> {

    private SimpMessagingTemplate messagingTemplate;

    // Setter for injecting the SimpMessagingTemplate.
    public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (messagingTemplate != null) {
            // Retrieve the formatted log message.
            String logMessage = eventObject.getFormattedMessage();
            // Send the log message to all subscribers on the /topic/logs destination.
            messagingTemplate.convertAndSend("/topic/logs", logMessage);
        }
    }
}

