package com.rits.workcenterservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Association {
    private String sequence;
    private String type;
    private String associateId;
    private String status;
    private boolean defaultResource;
}
