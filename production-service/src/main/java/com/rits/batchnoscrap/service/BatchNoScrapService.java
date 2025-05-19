package com.rits.batchnoscrap.service;

import com.rits.batchnoscrap.dto.BatchNoScrapQtyResponse;
import com.rits.batchnoscrap.dto.BatchNoScrapRequest;
import com.rits.batchnoscrap.model.BatchNoScrap;
import com.rits.batchnoscrap.model.BatchNoScrapMessageModel;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface BatchNoScrapService {
    BatchNoScrapMessageModel scrap(BatchNoScrapRequest batchNoScrapRequest) throws Exception;
    BatchNoScrapMessageModel unScrap(BatchNoScrapRequest batchNoScrapRequest) throws Exception;

    List<BatchNoScrap> retrieveAllScrap(String site) throws Exception;

    BatchNoScrapMessageModel delete(BatchNoScrapRequest batchNoScrapRequest) throws Exception;
    BatchNoScrapMessageModel retrieve(BatchNoScrapRequest batchNoScrapRequest) throws Exception;
    BatchNoScrapQtyResponse getBatchNoScrapByPhaseAndOperation(BatchNoScrapRequest request);
}
