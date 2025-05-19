package com.rits.scrapservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScrapRequestList {
    private List<ScrapRequest> scrapRequestList;
}
