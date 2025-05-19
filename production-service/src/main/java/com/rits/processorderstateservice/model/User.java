package com.rits.processorderstateservice.model;

import lombok.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    private List<String> site;
    private String user;
    private String status;
    private List<WorkCenter> workCenters;
}
