package com.rits.userservice.model;

import lombok.*;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Supervisors {
    private String current;
    private String validFrom;
    private String validTo;
    private String supervisedCcs;
}
