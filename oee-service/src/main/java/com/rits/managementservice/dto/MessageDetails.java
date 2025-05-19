package com.rits.managementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDetails {
    private String msg;
    private String msg_type;

    public void setFilterationData(List<Map<String, Object>> filterationDataList) {
        // Logic to set the filteration data
        // For this example, you can just set it directly as filterationData
        this.msg = filterationDataList.toString();
    }
}
