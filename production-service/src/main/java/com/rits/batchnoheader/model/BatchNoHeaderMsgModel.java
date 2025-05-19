package com.rits.batchnoheader.model;

import com.rits.nextnumbergeneratorservice.model.MessageDetails;
import lombok.*;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BatchNoHeaderMsgModel {
    private BatchNoHeader response;
    private MessageDetails message_details;
    private List<BatchNoHeader> responseList;
    private List<Map<String, String>> batchNos;
    private boolean existanceResponse;
}
