package com.rits.reasoncodeservice.dto;

import com.rits.reasoncodeservice.model.ReasonCode;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResponseList {
   private List<ReasonCodeResponse> reasonCodeResponseList;
}
