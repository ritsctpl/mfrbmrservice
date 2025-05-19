package com.rits.licencevalidationservice.repository;

import com.rits.licencevalidationservice.model.License;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LicenseRepository extends MongoRepository<License,String> {
    Optional<License> findByLicenseKeyIgnoreCase(String licenseKey);
}
