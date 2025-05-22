package com.rits.componentbuilderservice.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Column {
    private String title;
    private String type;
    private String dataIndex;
    private boolean required;
}
