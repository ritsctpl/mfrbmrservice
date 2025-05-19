package com.rits.mfrservice.repository;

import com.rits.mfrservice.dto.MFRResponse;
import com.rits.mfrservice.model.Mfr;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface MfrRepository extends MongoRepository<Mfr, String> {
  //  public boolean findBySiteAndProcedureNameAndActiveEquals(String site, String procedureName, int active);

    Boolean existsByMfrNoAndVersionAndSiteAndActive(String mfrNo, String version, String site, int i);

    int countByMfrNoAndVersionAndActive(String mfrNo, String version, int i);

    Mfr findByActiveAndMfrNoAndVersion(int i, String mfrNo, String version);

    Mfr findByMfrNoAndVersionAndSiteAndActive(String mfrNo, String version, String site, int i);

    List<MFRResponse> findTop50ByActiveAndSiteOrderByCreatedDateTimeDesc(int i, String site);
}
