package com.rits.oeeservice.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor

public class OeeRequestList {
    private List<OeeRequest> requests;

}
