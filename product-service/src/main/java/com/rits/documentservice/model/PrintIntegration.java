package com.rits.documentservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PrintIntegration {
    private String dataAcquisition;
    private String userArgument;
    private String formatting;
    private String transport;
    private boolean writeErrorLog;

}
