package com.rits.workinstructionservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Response {
    private String message;
    private String error;
    public Response(String message)
    {
        this.message=message;
    }
}
