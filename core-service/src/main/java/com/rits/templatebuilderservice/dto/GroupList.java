package com.rits.templatebuilderservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupList {
    private String handle;
    private String groupLabel;
}
