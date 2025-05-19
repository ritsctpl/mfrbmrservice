package com.rits.nonconformanceservice.repository;

import com.rits.nonconformanceservice.model.NcData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NonConformanceserviceRepository  extends MongoRepository<NcData,String> {
    public List<NcData> findByNcContextGboAndOperationBoAndResourceBo(String ncContextGbo, String operationBo, String resourceBo);
 /*   public NcData findBySecondaryNCDataNcCodeBoAndNcContextGbo(String ncContextGbo, String pcuBo);
    public List<NcData> findByNcCodeBoAndNcContextGbo(String ncCodeBo, String ncContextGbo);*/

    NcData findBySecondaryNCDataListNcCodeBoAndNcContextGbo(String ncContextGbo, String pcuBo);
    List<NcData> findByNcCodeBoAndNcContextGbo(String ncCodeBo, String ncContextGbo);

    List<NcData> findByNcContextGbo(String ncContextGbo);

    List<NcData> findByNcContextGboAndNcCodeBo(String ncContextGbo, String ncCodeBo);

//    List<NcData> findByActiveAndSiteAndNcContextGbo(int i, String site, String s);

    List<NcData> findBySiteAndNcContextGbo(String site, String s);
}
