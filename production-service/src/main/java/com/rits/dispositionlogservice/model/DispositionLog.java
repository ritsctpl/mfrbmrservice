package com.rits.dispositionlogservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Document(collection = "R_DISP_LOG")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DispositionLog {
    @Id
    private String handle;
    private String site;
    private String pcuBO;
    private String userBo;
    private String dateTime;
    private String qty;
    private String refDes;
    private String resourceBo;
    private String workCenterBo;
    private String itemBo;
    private String fromoperationBO;
    private String tooperationBO;
    private String stepID;
    private String fromRoutingBo;
    private String toRoutingBo;
    private String shopOrderBo;
    private String active;

}
