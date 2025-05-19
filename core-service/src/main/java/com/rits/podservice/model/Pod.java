package com.rits.podservice.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Document(collection = "R_POD_CONFIG")
public class Pod {
    @Id
    private String id;
    private String site;
    private String handle;
    private String type;
    private String podCategory;
    private String podName;
    private String panelLayout;
    private String description;
    private String status;
    private String displayDevice;
    private String displaySize;
    private String ncClient;
    private String realTimeMessageDisplay;
    private String specialInstructionDisplay;
    private boolean kafkaIntegration;
    private String kafkaId;
    private int sessionTimeout;
    private int refreshRate;
    private String defaultOperation;
    private String defaultResource;
    private String resourceStatus;
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
    private String createdBy;
    private String modifiedBy;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String subPod;
    private TabConfiguration tabConfiguration;

    private Settings settings;  // To hold settings as key-value pairs
    private List<Layout> layout;
    private String resourceType;
}

