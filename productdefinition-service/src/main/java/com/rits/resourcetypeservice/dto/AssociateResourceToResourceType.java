package com.rits.resourcetypeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssociateResourceToResourceType {
 private String resourceType;
 private List<String> resource;
 private String site;
}
