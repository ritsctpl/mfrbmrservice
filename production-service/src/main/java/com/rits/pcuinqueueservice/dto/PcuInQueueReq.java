    package com.rits.pcuinqueueservice.dto;

    import com.rits.pcuinqueueservice.model.PcuInQueueDetails;
    import lombok.*;

    import java.time.LocalDateTime;
    import java.util.List;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public class PcuInQueueReq {
        private String handle;
        private String site;
        private LocalDateTime dateTime;
        private String pcu;
        private String item;
        private String itemVersion;
        private String router;
        private String routerVersion;
        private String resource;
        private String operation;
        private String operationVersion;
        private String stepID;
        private String user;
        private String qtyToComplete;
        private String qtyInQueue;
        private String shopOrder;
        private String childRouter;
        private String childRouterVersion;
        private String parentStepID;
        private String workCenter;
        private Boolean disable;
        private int active;
        private List<PcuInQueueDetails> pcuList;
        private int recordLimit;
    }
