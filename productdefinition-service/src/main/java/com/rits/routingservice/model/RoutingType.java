package com.rits.routingservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "R_ROUTER")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RoutingType {
    private String site;
    private String routing;
    private String version;
    private String handle;
}
