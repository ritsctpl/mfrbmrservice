package com.rits.integration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IntegrationMessageModel {

    private JoltSpec response;
    private IntegrationEntity integrationEntityResponse;
    private MessageDetails message_details;

}
