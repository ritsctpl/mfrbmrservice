package com.rits.qualityacceptanceservice.repository;

import com.rits.lineclearanceservice.model.LineClearance;
import com.rits.qualityacceptanceservice.model.QualityAcceptance;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QualityAcceptanceRepository extends MongoRepository<QualityAcceptance,String> {

    QualityAcceptance findByHandleAndSiteAndActive(String handle, String site, int i);

}
