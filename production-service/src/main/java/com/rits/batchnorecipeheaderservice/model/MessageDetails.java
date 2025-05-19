package com.rits.batchnorecipeheaderservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MessageDetails {
    private String msg;
    private String msg_type;
}