package com.rits.worklistservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Printers {
    private String documentPrinter;
    private String labelPrinter;
    private String travelerPrinter;
}
