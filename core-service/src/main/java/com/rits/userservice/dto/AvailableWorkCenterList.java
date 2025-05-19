package com.rits.userservice.dto;

import com.rits.userservice.model.WorkCenter;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AvailableWorkCenterList {


    private List<AvailableWorkCenter> availableWorkCenterList;
   // private List<WorkCenter> workCenters = new ArrayList<>();

}
