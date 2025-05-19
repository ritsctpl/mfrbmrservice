package com.rits.podservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PodSelection {
    private String mainInput;
    private String mainInputHotKey;
    private String defaultOperation;
    private String defaultResource;
    private String sfcQueueButtonID;
    private String sfcInWorkButtonID;
    private String infoLine1;
    private String infoLine2;
    private boolean showOperationFirst;
    private boolean showQuantity;
    private boolean operationCanBeChanged;
    private boolean resourceCanBeChanged;
}
