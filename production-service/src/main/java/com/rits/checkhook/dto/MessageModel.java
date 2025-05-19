package com.rits.checkhook.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageModel {
    private boolean success;
    private String errorMsg;
}
