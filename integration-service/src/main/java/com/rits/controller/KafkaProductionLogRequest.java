package com.rits.controller;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class KafkaProductionLogRequest {
    private String notificationTopic;
    private String eventType;
}
