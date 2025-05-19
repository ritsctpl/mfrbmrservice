package com.rits.customdataformatservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomDataFormatResponseList {
    private List<CustomDataFormatResponse> customDataFormatList;
}
