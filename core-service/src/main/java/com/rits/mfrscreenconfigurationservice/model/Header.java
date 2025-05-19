package com.rits.mfrscreenconfigurationservice.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Header {
    private String title;
    private String type;
    private String reference;

}
