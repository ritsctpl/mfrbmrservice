package com.rits.cycletimeservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {
    private String shopOrder;
    private String routing;
    private String operation;
    private String resource;
    private String item;
    private String workCenter;
}
