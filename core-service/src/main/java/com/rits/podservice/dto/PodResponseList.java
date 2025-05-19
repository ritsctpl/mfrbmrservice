package com.rits.podservice.dto;

import com.rits.podservice.model.CustomData;
import com.rits.podservice.model.RButton;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class PodResponseList {
   private List<PodResponse> podList;


}
