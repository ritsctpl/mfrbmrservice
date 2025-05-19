//package com.rits.routingservice.model;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.List;
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class RoutingMessageModel {
//    private Routing response;
//    private String nextStepId;
//    private RoutingStep routingStep;
//    private Integer errorCode;
//    private MessageDetails message_details;
//}
package com.rits.routingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoutingMessageModel {
    private Routing response;
    private String nextStepId;
    private RoutingStep routingStep;
    private Integer errorCode;
    private MessageDetails message_details;
}
