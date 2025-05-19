package com.rits.processorderstateservice.model;

import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Resource {
    private String site;
    private String resource;
    private String status;
    private boolean processResource;
}