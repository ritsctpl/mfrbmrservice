package com.rits.datatypeservice.repository;

import com.rits.datatypeservice.dto.DataTypeResponse;
import com.rits.datatypeservice.model.DataType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DataTypeRepository extends MongoRepository<DataType,String> {

    boolean existsByDataTypeAndSiteAndActive(String dataType, String site, int active);

    long countByDataTypeAndCategoryAndSiteAndActive(String dataType, String category, String site, int active);

    DataType findByDataTypeAndCategoryAndActiveAndSite(String dataType, String category, int active, String site);

    DataType findByDataTypeAndCategoryAndSiteAndActive(String dataType, String category, String site, int active);

    DataType findByDataTypeAndCategoryAndActive(String dataType, String category, int active);

    Boolean existsByDataTypeAndCategoryAndSiteAndActive(String dataType, String category, String site, int active);

    List<DataTypeResponse> findByCategoryAndActiveAndSite(String category, int active, String site);

    List<DataTypeResponse> findByDataTypeContainingIgnoreCaseAndCategoryAndSiteAndActive(String dataType, String category, String site, int active);



    List<DataTypeResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int active, String site);
}
