package com.rits.dccollect.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserOptionList {
   private String  dcParameterRef;

           private String  optionName;

           private String optionValue;

           private int sequence;
}
