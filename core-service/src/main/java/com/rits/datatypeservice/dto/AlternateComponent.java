package com.rits.datatypeservice.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AlternateComponent {
    private String alternateComponent;
    private String alternateComponentVersion;
    private boolean enabled;
    private LocalDateTime validfromDateTime;
    private LocalDateTime validToDateTime;

}
