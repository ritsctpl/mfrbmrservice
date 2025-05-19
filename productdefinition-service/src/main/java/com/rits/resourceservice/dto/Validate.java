package com.rits.resourceservice.dto;

import lombok.*;
import org.springframework.http.ResponseEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Validate {
    private ResponseEntity<?> responseEntity;
}
