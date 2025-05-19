package com.rits.operationservice.model;

import com.rits.operationservice.dto.PodResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class PodResponseList {
   private List<PodResponse> podList;
}
