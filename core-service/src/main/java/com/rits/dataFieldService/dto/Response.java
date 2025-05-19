package com.rits.dataFieldService.dto;

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