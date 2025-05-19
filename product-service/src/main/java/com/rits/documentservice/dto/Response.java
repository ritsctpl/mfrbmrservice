package com.rits.documentservice.dto;

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