package com.rits.startservice.dto;

import lombok.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class StartRequestLists implements Serializable {
    private List<StartRequestDetails> requestList = new ArrayList<>();
}
