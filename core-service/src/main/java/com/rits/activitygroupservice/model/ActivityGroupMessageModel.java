package com.rits.activitygroupservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityGroupMessageModel {

    private ActivityGroup response;
    private MessageDetails message_details;
}
