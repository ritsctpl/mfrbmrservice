package com.rits.uomservice.dto;

import com.rits.uomservice.model.MessageDetails;
import com.rits.uomservice.model.UOMEntity;
import com.rits.uomservice.model.UomConvertionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UOMMessageModel {
    private UOMEntity response;
    private MessageDetails message_details;
    private UomConvertionEntity uomConvertionResponse;
    private List<UomConversionResponse> uomConvertions;

}
