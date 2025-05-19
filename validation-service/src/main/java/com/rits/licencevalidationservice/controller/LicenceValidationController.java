package com.rits.licencevalidationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.licencevalidationservice.service.LicenceValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("app/v1/licencevalidation-service")
public class LicenceValidationController {
    private final LicenceValidationService licenseService;
    private final ObjectMapper objectMapper;

   /* @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public License createLicense(@RequestBody LicenseRequest licenseRequest) {
        return licenseService.createLicense(licenseRequest.getUserId(), licenseRequest.getValidFrom(), licenseRequest.getValidTo());
    }*/
    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.CREATED)
    public boolean validateLicense(@RequestParam String licenseKey) {
        return licenseService.validateLicense(licenseKey);
    }

    @PostMapping("/test")
    @ResponseStatus(HttpStatus.CREATED)
    public boolean test() {
        return false;
    }

}
