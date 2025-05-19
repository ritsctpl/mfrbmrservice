package com.rits.podservice.dto;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PodResponse{
    private String podName;
    private  String description;
    private String status;
    private String defaultResource;
}

