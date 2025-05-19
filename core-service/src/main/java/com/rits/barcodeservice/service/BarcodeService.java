package com.rits.barcodeservice.service;

import com.rits.barcodeservice.dto.*;
import com.rits.barcodeservice.model.*;
import com.rits.barcodeservice.dto.Extension;
import com.rits.dataFieldService.dto.DataFieldRequest;

import java.util.List;


public interface BarcodeService {

    public String callExtension(Extension extension) throws Exception ;
    MessageModel createBarcode(BarcodeRequest barcodeRequest) throws Exception;


    MessageModel updateBarcode(BarcodeRequest barcodeRequest) throws Exception;


    MessageModel deleteCode(String site, String code,String userId) throws Exception;

    Barcode retrieveCode(String code, String site) throws Exception;

    BarcodeAllCodeList getAllCode(String site) throws Exception;

    public BarcodeResponse getCodeList(String code, String site);
}
