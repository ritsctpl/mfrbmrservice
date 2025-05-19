package com.rits.qualityservice.dto;

import com.rits.qualityservice.model.Quality;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class QualityRequestList {
    private List<QualityRequest> qualityRequest;
    private List<Quality> qualityResponseList;
}
