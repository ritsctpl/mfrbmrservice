package com.rits.customdataformatservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Builder
public class MainCustomDataObject {

    public List<CustomDataObject> customdataList;

    @JsonCreator
    public MainCustomDataObject(@JsonProperty("customdataList") List<CustomDataObject> customdataList) {
        this.customdataList = customdataList;
    }


}
