package com.rits.mfrrecipesservice.dto;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class MFRResponseList {


    private List<MFRResponse> mfrList;
}
