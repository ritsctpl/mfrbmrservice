package com.rits.oeeservice.repository;

import com.rits.oeeservice.model.ApiConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApiConfigurationRepository extends JpaRepository<ApiConfiguration, Long> {
    Optional<ApiConfiguration> findByApiName(String apiName);
}
