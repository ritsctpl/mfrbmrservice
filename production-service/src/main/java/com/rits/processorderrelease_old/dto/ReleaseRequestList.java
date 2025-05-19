package com.rits.processorderrelease_old.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReleaseRequestList {
    private List<ReleaseRequest> releaseRequest;

}
