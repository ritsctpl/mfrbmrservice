package com.rits.bomservice.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AlternateComponent {
    private String alternateComponent;
    private String alternateComponentVersion;
    private boolean enabled;
    private String validfromDateTime;
    private String validToDateTime;

}
