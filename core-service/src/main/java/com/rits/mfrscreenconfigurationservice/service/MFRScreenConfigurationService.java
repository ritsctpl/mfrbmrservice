package com.rits.mfrscreenconfigurationservice.service;

import com.rits.mfrscreenconfigurationservice.dto.ProductResponseList;
import com.rits.mfrscreenconfigurationservice.model.MFRScreenConfiguration;
import com.rits.mfrscreenconfigurationservice.model.MessageModel;
import com.rits.mfrscreenconfigurationservice.dto.MfrScreenConfigurationRequest;

public interface MFRScreenConfigurationService {

    MessageModel createMfrScreenConfiguration(MfrScreenConfigurationRequest mfrScreenConfigurationRequest);

    MFRScreenConfiguration retrieveProduct(MfrScreenConfigurationRequest mfrScreenConfigurationRequest);

    MessageModel deleteProductName(MfrScreenConfigurationRequest mfrScreenConfigurationRequest);

    ProductResponseList getProductListListByCreationDate(MfrScreenConfigurationRequest mfrScreenConfigurationRequest);
}
