package com.rits.sectionbuilderservice.repository;

import com.rits.sectionbuilderservice.dto.SectionResponse;
import com.rits.sectionbuilderservice.model.SectionBuilder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SectionBuilderRepository extends MongoRepository<SectionBuilder, String> {
    SectionBuilder findByHandleAndSiteAndActive(String handle, String site, int i);
    List<SectionResponse> findBySiteAndSectionLabelContainingIgnoreCaseAndActiveEquals(String site, String sectionLabel, int i);
    List<SectionResponse> findTop50BySiteAndActive(String site, int active);
    boolean existsBySiteAndActiveAndSectionLabel(String site, int i, String sectionLabel);
}
