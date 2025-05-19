package com.rits.startservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuList {
    private List<StartRequestDetails> pcuList;
}
