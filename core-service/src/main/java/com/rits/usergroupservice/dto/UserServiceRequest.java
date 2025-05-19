package com.rits.usergroupservice.dto;

import lombok.*;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserServiceRequest {
    private List<String> site;
}
