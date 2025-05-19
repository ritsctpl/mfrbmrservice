package com.rits.datacollectionservice.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentPoint {
    private Map<String, String> attachmentList;
}

