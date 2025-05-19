package com.rits.resourcetypeservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AssociateResourceTypeToResource {
   private String site;
   private List<String> resourceType;
   private String resource;
}
