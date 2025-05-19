package com.rits.controller;

import com.rits.kafkapojo.TopicConfiguration;
import com.rits.kafkaservice.TopicConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/v1/integration-service/topic-configurations")
public class TopicConfigurationController {

    private final TopicConfigurationService service;


    @GetMapping("/getAllConfigurations")
    public List<TopicConfiguration> getAllConfigurations() {
        return service.getAllConfigurations();
    }

    @GetMapping("/{id}")
    public TopicConfiguration getConfigurationById(@PathVariable String id) {
        return service.getConfigurationById(id)
                .orElseThrow(() -> new RuntimeException("Topic Configuration not found with ID: " + id));
    }

    @PostMapping("/createConfiguration")
    public TopicConfiguration createConfiguration(@RequestBody TopicConfiguration configuration) {
        return service.createConfiguration(configuration);
    }

    @PutMapping("/{id}")
    public TopicConfiguration updateConfiguration(@PathVariable String id, @RequestBody TopicConfiguration configuration) {
        return service.updateConfiguration(id, configuration);
    }

    @DeleteMapping("/{id}")
    public void deleteConfiguration(@PathVariable String id) {
        service.deleteConfiguration(id);
    }
}
