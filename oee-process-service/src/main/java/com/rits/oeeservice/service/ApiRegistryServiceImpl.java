package com.rits.oeeservice.service;

import com.rits.oeeservice.model.ApiConfiguration;
import com.rits.oeeservice.repository.ApiConfigurationRepository;
import com.rits.oeeservice.service.ApiRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiRegistryServiceImpl implements ApiRegistryService {

    @Autowired
    private ApiConfigurationRepository apiConfigurationRepository;

    @Override
    public ApiConfiguration getApiConfiguration(String apiName) {
        return apiConfigurationRepository.findByApiName(apiName).orElse(null);
    }
}
