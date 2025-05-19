package com.rits.shoporderrelease.event;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class KafkaShopOrderRequest {
    private String notificationTopic;
    private String eventType;
}