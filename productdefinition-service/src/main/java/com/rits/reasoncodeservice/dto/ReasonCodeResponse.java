package com.rits.reasoncodeservice.dto;

import com.rits.reasoncodeservice.model.CustomData;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReasonCodeResponse {
    private String reasonCode;
    private String description;
    private String category;
    private String status;
}
