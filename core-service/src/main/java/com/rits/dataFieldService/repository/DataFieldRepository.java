package com.rits.dataFieldService.repository;

import com.rits.dataFieldService.dto.DataFieldResponse;
import com.rits.dataFieldService.model.DataField;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DataFieldRepository extends MongoRepository<DataField, String> {
    public List<DataFieldResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);

    public List<DataFieldResponse> findByActiveAndSiteAndDataFieldContainingIgnoreCase(int active, String site, String dataField);

    public boolean existsByActiveAndSiteAndDataField(int active, String site, String dataField);

    public DataField findByActiveAndSiteAndDataField(int active, String site, String dataField);

    boolean existsByActiveAndSiteAndDataFieldAndTrackable(int i, String site, String dataField, boolean b);
}
