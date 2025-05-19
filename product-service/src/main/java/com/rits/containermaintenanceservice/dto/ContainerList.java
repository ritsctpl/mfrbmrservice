package com.rits.containermaintenanceservice.dto;

import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ContainerList {
    List<Container> containerList;
}
