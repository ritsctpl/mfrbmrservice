package com.rits.checkhook.dto;

import com.rits.dccollect.dto.Attachment;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataCollectionRequest {
    private String site;
    private String pcu;
    private String operation;
    private String resource;

    private List<Attachment> attachmentList;
}
