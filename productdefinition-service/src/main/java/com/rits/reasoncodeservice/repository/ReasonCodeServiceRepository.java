package com.rits.reasoncodeservice.repository;

import com.rits.reasoncodeservice.dto.ReasonCodeResponse;
import com.rits.reasoncodeservice.model.ReasonCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface ReasonCodeServiceRepository extends MongoRepository<ReasonCode,String> {
    Boolean existsBySiteAndActiveAndReasonCode(String site, int i, String reasonCode);

    ReasonCode findBySiteAndActiveAndReasonCode(String site, int i, String reasonCode);

    List<ReasonCodeResponse> findBySiteAndActive(String site, int i);

    List<ReasonCodeResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int i, String site);
}
