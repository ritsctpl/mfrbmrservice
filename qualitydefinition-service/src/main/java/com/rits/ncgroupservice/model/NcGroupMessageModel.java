package com.rits.ncgroupservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NcGroupMessageModel {
    private NcGroup response;
    private MessageDetails message_details;
}
