package com.rits.workinstructionservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Column {
    private String columnSequence;
    private String columnName;
    private Detail details;

}
