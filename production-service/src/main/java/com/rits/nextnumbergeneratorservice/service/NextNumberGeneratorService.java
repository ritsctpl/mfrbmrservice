package com.rits.nextnumbergeneratorservice.service;

import com.rits.nextnumbergeneratorservice.dto.*;
import com.rits.nextnumbergeneratorservice.model.NextNumberMessageModel;
import com.rits.nextnumbergeneratorservice.model.NextNumberGenerator;

import java.util.List;

public interface NextNumberGeneratorService {

    public NextNumberMessageModel create(NextNumberGeneratorRequest nextNumberGeneratorRequest) throws Exception;

    public NextNumberMessageModel updateNextNumber(NextNumberGeneratorRequest nextNumberGeneratorRequest) throws Exception;

    public NextNumberGenerator retrieveNextNumber(NextNumberGeneratorRequest nextNumberGeneratorRequest)throws Exception;

    public String sampleNextNumberOnCreate(NextNumberGeneratorRequest nextNumberGeneratorRequest)throws Exception;

    public NextNumberMessageModel delete(NextNumberGeneratorRequest nextNumberGeneratorRequest) throws Exception;

    public String generatePrefixAndSuffix(GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest) throws Exception;

    public NextNumberMessageModel generateNextNumber(GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest)throws Exception;

    GeneratedNextNumber updateCurrentSequence(GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest)throws Exception;

    NextNumberMessageModel getAndUpdateCurrentSequence(GeneratePrefixAndSuffixRequest generatePrefixAndSuffixRequest)throws Exception;

    List<NextnumberList> getNewInventory(String site, String object, String objectVersion, double size, String userBO, String nextNumberActivity, String numberType)throws Exception;

    List<NextNumberResponse> createNextNumberList(String numberType, String site, String object, String objectVersion, String shopOrder, String pcu, String ncBo, String userBo, double batchQty) throws Exception;

    List<NextNumberResponse> createNextNumbers(String numberType, String site, String object, String objectVersion, String shopOrder, String pcu, String ncBo, String userBo, double batchQty) throws Exception;
}
