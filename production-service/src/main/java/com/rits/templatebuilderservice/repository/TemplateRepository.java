package com.rits.templatebuilderservice.repository;

import com.rits.templatebuilderservice.dto.TemplateResponse;
import com.rits.templatebuilderservice.model.Template;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TemplateRepository extends MongoRepository<Template, String> {
    Boolean existsByHandleAndSiteAndActiveEquals(String handle, String site, int active);

    Template findBySiteAndHandleAndActiveEquals(String site, String handle, int active);

    List<TemplateResponse> findBySiteAndTemplateLabelContainingIgnoreCaseAndActiveEqualsOrderByCreatedDateTimeDesc(String site,String templateLabel, int active);

    List<TemplateResponse> findTop50BySiteAndActiveEqualsOrderByCreatedDateTimeDesc(String site, int active);

    List<Template> findBySiteAndTemplateLabelAndCurrentVersionAndActiveEquals(String site, String templateLabel, Boolean currentVersion, int active);
}
