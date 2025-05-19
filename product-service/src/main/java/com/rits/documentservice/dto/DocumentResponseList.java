package com.rits.documentservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DocumentResponseList {
    private List<DocumentResponse> documentResponseList;
}
