package com.rits.assyservice.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Document(collection = "R_ASSY_DATA")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AssyData {
    private String _id;
    private String pcuBO;
    private String site;
    private String pcuBomBO;
    private String shopOrderBO;
    private String itemBO;
    private String pcuRouterBO;
    private String tags;
    private String parentOrderBO;
    private String parentPcuBO;
    private int level;
    private List<Component> componentList;
    private List<Ancestry> ancestry;
    private int active;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Component {
        private String sequence;
        private String component;
        private String componentVersion;
        private String qty;
        private String inventoryBO;
        private String assembledBy;
        private boolean removed;
        private String operation;
        private String resourceBO;
        private boolean nonBom;
        private LocalDateTime removedDate;
        private String removedBy;
        private String removedOperationBO;
        private String removedResourceBO;
        private List<AssyData> childAssembly;
        private List<AssemblyData> assemblyDataList;
        private LocalDateTime createdDateTime;
        private LocalDateTime updatedDateTime;
        private List<AssembledPcu> assembledPcu;
        private int active;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class AssemblyData {
        private String sequence;
        private String dataField;
        private String value;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class AssembledPcu {
        private String pcuBo;
    }



    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Ancestry {
        private String item;
        private String pcuBO;
    }
}