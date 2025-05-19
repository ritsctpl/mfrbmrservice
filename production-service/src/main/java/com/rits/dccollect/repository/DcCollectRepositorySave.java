package com.rits.dccollect.repository;

import com.rits.dccollect.dto.DcSaveParametricMeasures;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DcCollectRepositorySave extends MongoRepository<DcSaveParametricMeasures,String> {
    List<DcSaveParametricMeasures> findByActiveAndSiteAndDataCollectionAndVersion(int i, String site, String dataCollection, String version);

    List<DcSaveParametricMeasures> findByActiveAndSiteAndParametricMeasuresListPcuBO(int active,String site,String pcuBO);

    List<DcSaveParametricMeasures> findByActiveAndSiteAndParametricMeasuresListPcuBOAndDataCollectionAndVersion(int active,String site,String pcuBO,String dataCollection, String version);

    List<DcSaveParametricMeasures> findTop1ByActiveAndSiteAndDataCollectionAndVersionAndParametricMeasuresList_PcuBOOrderByCreatedDateTimeDesc(int i, String site, String dataCollection, String version, String s);

    boolean existsByActiveAndSiteAndParametricMeasuresListPcuBOAndDataCollectionAndVersion(int active,String site,String pcuBO,String dataCollection, String version);

    Integer countByActiveAndSiteAndHandleContaining(int active,String site, String handle);
}
