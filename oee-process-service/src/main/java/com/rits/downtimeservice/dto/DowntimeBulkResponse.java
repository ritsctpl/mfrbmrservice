package com.rits.downtimeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DowntimeBulkResponse {
    private String message;
    private boolean success;
    private int loggedCount;

}
