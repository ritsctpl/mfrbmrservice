package com.rits.scrapservice.dto;

import lombok.*;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class RetrieveRequest {
    private String site;
    private List<String> objectList;
}
