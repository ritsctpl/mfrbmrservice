package com.rits.oeeservice.service;

import com.rits.oeeservice.model.ApiConfiguration;
import com.rits.oeeservice.repository.ApiConfigurationRepository;
import com.rits.oeeservice.service.ApiConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ApiConfigurationServiceImpl implements ApiConfigurationService {

    @Autowired
    private ApiConfigurationRepository apiConfigurationRepository;

    @Override
    public ApiConfiguration createApiConfiguration(ApiConfiguration configuration) {
        return apiConfigurationRepository.save(configuration);
    }

    @Override
    public ApiConfiguration updateApiConfiguration(Long id, ApiConfiguration configuration) {
        Optional<ApiConfiguration> existing = apiConfigurationRepository.findById(id);
        if (existing.isPresent()) {
            ApiConfiguration apiConfig = existing.get();
            apiConfig.setApiName(configuration.getApiName());
            apiConfig.setStoredProcedure(configuration.getStoredProcedure());
            apiConfig.setHttpMethod(configuration.getHttpMethod());
            apiConfig.setInputParameters(configuration.getInputParameters());
            apiConfig.setOutputStructure(configuration.getOutputStructure());
            return apiConfigurationRepository.save(apiConfig);
        }
        return null;
    }

    @Override
    public void deleteApiConfiguration(Long id) {
        apiConfigurationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<ApiConfiguration> getAllApiConfigurations() {
        List<ApiConfiguration> configs = apiConfigurationRepository.findAll();
        // Force LOB initialization
        configs.forEach(config -> {
            config.getInputParameters();
            config.getOutputStructure();
        });
        return configs;
    }

    @Override
    @Transactional
    public ApiConfiguration getApiConfigurationById(Long id) {
        ApiConfiguration config = apiConfigurationRepository.findById(id).orElse(null);
        if (config != null) {
            // Force LOB initialization within transaction
            config.getInputParameters();
            config.getOutputStructure();
        }
        return config;
    }
}
