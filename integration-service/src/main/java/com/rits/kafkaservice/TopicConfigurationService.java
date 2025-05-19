package com.rits.kafkaservice;

import com.rits.kafkapojo.TopicConfiguration;
import com.rits.repository.TopicConfigurationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TopicConfigurationService {

    private final TopicConfigurationRepository repository;

    public TopicConfigurationService(TopicConfigurationRepository repository) {
        this.repository = repository;
    }

    public List<TopicConfiguration> getAllConfigurations() {
        return repository.findAll();
    }

    public Optional<TopicConfiguration> getConfigurationById(String id) {
        return repository.findById(id);
    }

    public TopicConfiguration createConfiguration(TopicConfiguration configuration) {
        return repository.save(configuration);
    }

    public TopicConfiguration updateConfiguration(String id, TopicConfiguration updatedConfiguration) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setTopicName(updatedConfiguration.getTopicName());
                    existing.setActive(updatedConfiguration.isActive());
                    existing.setApiUrl(updatedConfiguration.getApiUrl());
                    return repository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Topic Configuration not found with ID: " + id));
    }

    public void deleteConfiguration(String id) {
        repository.deleteById(id);
    }
}
