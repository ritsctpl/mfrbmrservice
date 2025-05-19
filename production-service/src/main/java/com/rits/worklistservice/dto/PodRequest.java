package com.rits.worklistservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class PodRequest {
    private String site;
    private String type;
    private String podName;
    private String description;
    private String status;
    private String displayDevice;
    private String displaySize;
    private String ncClient;
    private String realTimeMessageDisplay;
    private String specialInstructionDisplay;
    private String panelLayout;
    private boolean kafkaIntegration;
    private String kafkaId;
    private int sessionTimeout;
    private int refreshRate;
    private String defaultOperation;
    private String defaultResource;
    private String defaultPhaseId;
    private boolean soundWithErrorMessage;
    private boolean showHomeIcon;
    private boolean showHelpIcon;
    private boolean showLogoutIcon;
    private boolean autoExpandMessageArea;
    private boolean operationCanBeChanged;
    private boolean resourceCanBeChanged;
    private boolean phaseCanBeChanged;
    private boolean showQuantity;
    private boolean showResource;
    private boolean showOperation;
    private boolean showPhase;
    private String pcuQueueButtonId;
    private String pcuInWorkButtonId;
    private String documentName;
    private List<RButton> buttonList;
    private List<ListOption> listOptions;
    private List<PodSelection> podSelection;
    private List<Printers>  printers;
    private List<CustomData> customDataList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userId;
    private String subPod;
    private TabConfiguration tabConfiguration;
    private String podCategory;
    private Settings settings;  // To hold settings as key-value pairs
    private List<Layout> layout;


}
