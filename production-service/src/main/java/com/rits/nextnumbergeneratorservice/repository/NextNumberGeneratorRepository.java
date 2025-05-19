package com.rits.nextnumbergeneratorservice.repository;

import com.rits.nextnumbergeneratorservice.model.NextNumberGenerator;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NextNumberGeneratorRepository extends MongoRepository<NextNumberGenerator,String> {
    NextNumberGenerator findByActiveAndSiteAndNumberTypeAndObjectAndDefineBy(int i, String site, String numberType, String object, String defineBy);
    NextNumberGenerator findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersionAndDefineBy(int i, String site, String numberType, String object, String objectVersion, String defineBy);

    NextNumberGenerator findByActiveAndSiteAndNumberTypeAndObjectAndOrderType(int i, String site, String numberType, String object, String orderType);

    NextNumberGenerator findByActiveAndSiteAndNumberType(int i, String site, String numberType);

    NextNumberGenerator findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersion(int i, String site, String numberType, String object, String objectVersion);
    NextNumberGenerator findByActiveAndSiteAndNumberTypeAndObjectAndObjectVersionAndOrderType(int i, String site, String numberType, String object, String objectVersion, String orderType);
}
