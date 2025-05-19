package com.rits.oeeservice.service;

import com.rits.oeeservice.model.ApiConfiguration;

public interface ApiRegistryService {
    ApiConfiguration getApiConfiguration(String apiName);
}
