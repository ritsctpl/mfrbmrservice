package com.rits.podservice.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDetails {
    private String msg;
    private String msg_type;
}
