package com.rits.bomheaderservice.repository;

import com.rits.bomheaderservice.model.BomHeader;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BomHeaderRepository extends MongoRepository<BomHeader,String> {

    BomHeader findBySiteAndActiveAndPcuBomBO(String site, int active, String pcuBO);

    boolean existsBySiteAndActiveAndPcuBomBO(String site,int active, String pcuBomBO);

    BomHeader findByActiveAndSiteAndPcuBO(int i, String site, String pcuBO);
}
