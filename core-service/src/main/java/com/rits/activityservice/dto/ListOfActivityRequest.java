package com.rits.activityservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListOfActivityRequest {
        private List<String> activityId;
        private String activity;
        private List<String> activityGroup;
        //private String  site;
        private String currentSite;
        private String type;
    }

