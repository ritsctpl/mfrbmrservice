package com.rits.dccollect.repository;

import com.rits.dccollect.dto.DcParametricPreSave;
import com.rits.dccollect.model.ParametricPreSave;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DcCollectRepositorySaveDraft extends MongoRepository<DcParametricPreSave,String> {
    DcParametricPreSave findByActiveAndSiteAndDataCollectionAndVersion(int i, String site, String dataCollection, String version);

    DcParametricPreSave findByActiveAndSiteAndHandle(int i, String site, String handle);
}
