package com.rits.sectionbuilderservice.service;

import com.rits.sectionbuilderservice.dto.SectionBuilderRequest;
import com.rits.sectionbuilderservice.model.MessageModel;
import com.rits.sectionbuilderservice.model.SectionBuilder;

import java.util.List;

public interface SectionBuilderService {
    MessageModel create(SectionBuilderRequest request);

    MessageModel update(SectionBuilderRequest request);

    MessageModel delete(SectionBuilderRequest request);

    SectionBuilder retrieve(SectionBuilderRequest request);

    List<SectionBuilder> retrieveAll(String site);
    List<SectionBuilder> retrieveTop50(String site);
    boolean isSectionBuilderExist(String site, String sectionLabel);
}
