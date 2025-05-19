package com.rits.groupbuilderservice.service;

import com.rits.groupbuilderservice.dto.GroupBuilderRequest;
import com.rits.groupbuilderservice.model.GroupBuilder;
import com.rits.groupbuilderservice.model.MessageModel;

import java.util.List;

public interface GroupBuilderService {
    MessageModel create(GroupBuilderRequest request);

    MessageModel update(GroupBuilderRequest request);

    MessageModel delete(GroupBuilderRequest request);

    GroupBuilder retrieve(GroupBuilderRequest request);

    List<GroupBuilder> retrieveAll(String site);
    List<GroupBuilder> retrieveTop50(String site);
    boolean isGroupBuilderExist(String site, String groupLabel);
}
