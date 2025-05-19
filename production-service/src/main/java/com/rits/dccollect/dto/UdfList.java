package com.rits.dccollect.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UdfList {
     private String  dcParameterRef;

             private String  prompt;

             private String  dataType;

             private boolean required;

             private int sequence;
}
