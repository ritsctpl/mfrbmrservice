package com.rits.customdataformatservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rits.customdataformatservice.model.MainCustomDataObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class CustomDataFormatObject {
    private List<MainCustomDataObject> mainCustomDataObjectList;

    @JsonCreator
    public CustomDataFormatObject(@JsonProperty("mainCustomDataObjectList") List<MainCustomDataObject> mainCustomDataObjectList) {
        this.mainCustomDataObjectList = mainCustomDataObjectList;
    }
}
