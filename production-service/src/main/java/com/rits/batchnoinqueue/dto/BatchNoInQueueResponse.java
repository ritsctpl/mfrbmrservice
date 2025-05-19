package com.rits.batchnoinqueue.dto;

import com.rits.batchnoinqueue.model.BatchNoInQueue;
import com.rits.batchnoinwork.model.BatchNoInWork;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BatchNoInQueueResponse {
    private List<BatchNoInQueue> batchNoInQueueList;
    private List<BatchNoInWork> batchNoInWorkList;
    private List<BatchInQueueResponse> batchNoResponse;
}
