package com.rits.itemservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Response {
    private String message;
    private String error;
}
