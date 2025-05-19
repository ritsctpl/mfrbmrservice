package com.rits.mfrrecipesservice.dto;

import com.rits.activitygroupservice.model.MessageDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NextNumberMessageModel {
    private MessageDetails message_details;
}
