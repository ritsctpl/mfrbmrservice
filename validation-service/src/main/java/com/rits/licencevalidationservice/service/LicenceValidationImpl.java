package com.rits.licencevalidationservice.service;

import com.rits.licencevalidationservice.model.License;
import com.rits.licencevalidationservice.repository.LicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class LicenceValidationImpl implements LicenceValidationService {
    private final LicenseRepository licenseRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${microservices.shutdown.urls}")
    private List<String> microservicesShutdownUrls;

    public boolean validateLicense(String licenseKey) {
        Optional<License> licenseOpt = licenseRepository.findByLicenseKeyIgnoreCase(licenseKey);
        List<License> licenseList = licenseRepository.findAll();

        if (licenseOpt.isPresent()) {
            License license = licenseOpt.get();
            return license.isActive() && license.getValidTo().after(new Date());
        } else {
            shutdownMicroservices(microservicesShutdownUrls);
        }
        return false;
    }

    private void shutdownMicroservices(List<String> urls) {
        for (String url : urls) {
            String shutdownUrl=url+"/shutdown";
            try {
                webClientBuilder.build()
                        .post()
                        .uri(shutdownUrl)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            } catch (WebClientResponseException ex) {
                if (ex.getStatusCode().is5xxServerError()) {
                    System.err.println("Service unavailable at URL: " + url + ". Skipping to the next.");
                } else {
                    throw ex;
                }
            } catch (Exception ex) {
                System.err.println("An error occurred while shutting down service at URL: " + url);
                ex.printStackTrace();
            }
        }}

    private String generateLicenseKey() {
        return UUID.randomUUID().toString();
    }

}
