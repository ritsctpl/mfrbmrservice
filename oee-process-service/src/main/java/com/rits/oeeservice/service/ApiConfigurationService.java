package com.rits.oeeservice.service;

import com.rits.oeeservice.model.ApiConfiguration;
import java.util.List;

public interface ApiConfigurationService {
    ApiConfiguration createApiConfiguration(ApiConfiguration configuration);
    ApiConfiguration updateApiConfiguration(Long id, ApiConfiguration configuration);
    void deleteApiConfiguration(Long id);
    List<ApiConfiguration> getAllApiConfigurations();
    ApiConfiguration getApiConfigurationById(Long id);
}
