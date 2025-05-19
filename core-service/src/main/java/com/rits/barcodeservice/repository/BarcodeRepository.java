package com.rits.barcodeservice.repository;

import com.rits.barcodeservice.dto.BarcodeAllCodeResponse;
import com.rits.barcodeservice.dto.BarcodeResponseList;
import com.rits.barcodeservice.model.Barcode;
import com.rits.dataFieldService.dto.DataFieldResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BarcodeRepository extends MongoRepository<Barcode, String> {
    public  boolean existsByActiveAndSiteAndCode(int Active, String site, String code);

    public Barcode findByActiveAndSiteAndCode(int i, String site, String code);

    List<DataFieldResponse> findByActiveAndSiteAndCodeContainingIgnoreCase(int i, String site, String code);

    List<BarcodeAllCodeResponse> findByActiveAndSite(int i, String site);
}
