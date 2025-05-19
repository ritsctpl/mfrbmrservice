package com.rits.licencevalidationservice.service;

import com.rits.licencevalidationservice.repository.LicenseRepository;
import org.springframework.context.MessageSource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.UUID;

public interface LicenceValidationService {
    public boolean validateLicense(String licenseKey);
}
