package com.rits.extensionservice.service;

import com.rits.extensionservice.dto.ExtensionRequest;

public interface ExtensionService {
    public String createExtension(ExtensionRequest extensionRequest) throws Exception;
}
