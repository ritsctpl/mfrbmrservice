package com.rits.batchnohold.model;

import com.rits.batchnoscrap.model.BatchNoScrap;
import com.rits.scrapservice.model.MessageDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchNoHoldMessageModel {
    private BatchNoHold batchNoHoldResponse;
    private MessageDetails message_details;

}
