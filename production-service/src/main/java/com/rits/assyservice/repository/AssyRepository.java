package com.rits.assyservice.repository;

import com.rits.assyservice.model.AssyData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AssyRepository extends MongoRepository<AssyData, String> {
    AssyData findByActiveAndSiteAndPcuBOAndItemBO(int i, String site, String pcuBO, String item);

    AssyData findByActiveAndSiteAndPcuBO(int i, String site, String pcuBO);
    List<AssyData> findByActiveAndSiteAndAncestry_PcuBOAndAncestry_Item(int i, String site, String pcuBO, String item);
}