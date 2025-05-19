package com.rits.downtimeservice.model;

import com.rits.downtimeservice.dto.DowntimeLiveRecord;
import com.rits.downtimeservice.dto.MachineLogEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DowntimeMessageModel {

    private MachineLogEntity response;
    private MessageDetails message_details;
    private List<DowntimeLiveRecord> downtimeRecordList;
}
