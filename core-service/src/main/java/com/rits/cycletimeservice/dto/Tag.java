package com.rits.cycletimeservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    private String tag;
    private String priorityCombination;
}
