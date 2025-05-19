package com.rits.listmaintenceservice.model;

import lombok.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Detail {
    private List<Status> statusList;
}
