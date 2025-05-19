package com.rits.scrapservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScrapRequestListDetails {
    private List<ScrapRequestDetails> scrapRequestList;
}
