package com.rits.bmrservice.dto;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class BMRResponseList {
    private List<BMRResponse> bmrList;
}
