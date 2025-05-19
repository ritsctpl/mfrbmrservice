package com.rits.pcurouterheaderservice.repository;

import com.rits.pcurouterheaderservice.model.PcuRouterHeader;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PcuRouterHeaderRepository extends MongoRepository<PcuRouterHeader, String> {


    boolean existsByActiveAndSiteAndPcuBo(int active, String site, String pcuBo);

    PcuRouterHeader findByActiveAndSiteAndPcuBo(int active, String site, String pcuBo);




}
