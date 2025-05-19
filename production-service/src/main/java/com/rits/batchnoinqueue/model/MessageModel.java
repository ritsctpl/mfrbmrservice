package com.rits.batchnoinqueue.model;

import com.rits.batchnoinqueue.dto.BatchNoInQueueRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageModel {
    private BatchNoInQueue response;
    private MessageDetails message_details;
}
