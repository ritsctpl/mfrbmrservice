package com.rits.processorderrelease_old.event;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class KafkaProcessOrderRequest {
    private String notificationTopic;
    private String eventType;
}