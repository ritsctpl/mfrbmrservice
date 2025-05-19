package com.rits.pcudoneservice.repository;

import com.rits.pcudoneservice.model.PcuDone;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PcuDoneRepository extends MongoRepository<PcuDone,String> {
    boolean existsByActiveAndSiteAndPcuBO(int i, String site, String pcuBO);

    PcuDone findByActiveAndSiteAndHandle(int i, String site, String s);

    PcuDone findByActiveAndSiteAndPcuBO(int i, String site, String pcuBO);
}
