package com.rits.customdataformatservice.repository;


import com.rits.customdataformatservice.dto.CustomDataFormatResponse;
import com.rits.customdataformatservice.model.CustomDataFormat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CustomDataFormatRepository extends MongoRepository<CustomDataFormat, String> {

    boolean existsByActiveAndCharacter(int active, String character);

    CustomDataFormat findByCodeAndSiteAndSequence(String code, String site, int sequence);

    List<CustomDataFormatResponse> findByActiveAndSiteOrderByCreatedDateTimeDesc(int i, String site);

    List<CustomDataFormat> findByCodeAndSiteAndActiveOrderBySequence(String code, String site, int active);

    CustomDataFormat findByActiveAndSiteAndDataField(int i, String site, String strippingEnd);

    CustomDataFormat findByActiveAndCodeAndSiteAndDataField(int i, String format, String site, String replaceable);
}
