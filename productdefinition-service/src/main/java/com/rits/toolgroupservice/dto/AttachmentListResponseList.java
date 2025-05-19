package com.rits.toolgroupservice.dto;

import com.rits.toolgroupservice.model.Attachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachmentListResponseList {

    private List<AttachmentListResponse> attachmentList;
}
