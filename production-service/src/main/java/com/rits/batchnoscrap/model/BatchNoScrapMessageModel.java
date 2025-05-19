package com.rits.batchnoscrap.model;

import com.rits.scrapservice.model.MessageDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchNoScrapMessageModel {
    private BatchNoScrap batchNoScrapResponse;
    private List<BatchNoScrap> batchNoScrapList;
    private MessageDetails message_details;

}
