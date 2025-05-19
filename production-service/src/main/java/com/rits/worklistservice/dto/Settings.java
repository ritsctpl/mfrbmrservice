package com.rits.worklistservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Settings {

    private String pcuorBatchNumberBrowseActivity;
    private String podWorkListActivity;
    private String buttonLocation;

    private TabOutConfig onTabOutOfOperation;
    private TabOutConfig onTabOutOfWorkCenter;
    private TabOutConfig onTabOutOfResource;

    private UpdateFieldsToSession updateFieldsToSession;

    private List<String> buttonActivityInWork;
    private List<String> buttonActivityInQueue;

    // TabOutConfig class to represent the reloadWorkList and reloadResource configuration
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TabOutConfig {
        private boolean reloadWorkList;
        private boolean reloadResource;
    }

    // UpdateFieldsToSession class to represent the configuration of session field updates
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UpdateFieldsToSession {
        private boolean operation;
        private boolean resource;
        private boolean batchNumber;
        private boolean order;
        private boolean recipe;
        private boolean item;
    }
}
