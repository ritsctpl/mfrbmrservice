package com.rits.qualityacceptanceservice.service;

import com.rits.qualityacceptanceservice.dto.QualityAcceptanceRequest;
import com.rits.qualityacceptanceservice.model.MessageModel;

import java.util.List;

public interface QualityAcceptanceService {

    MessageModel create(QualityAcceptanceRequest request);

}
