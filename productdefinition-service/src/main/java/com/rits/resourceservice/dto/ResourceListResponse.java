package com.rits.resourceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResourceListResponse {
    private String resource;
    private String description;
    private String status;
//	private String resourceType;
}
