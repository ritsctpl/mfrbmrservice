package com.rits.workinstructionservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchRecipeInstruction {
    private String text;
    private  String url;
}
