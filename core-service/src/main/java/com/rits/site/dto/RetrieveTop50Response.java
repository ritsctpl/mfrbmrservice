package com.rits.site.dto;

import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RetrieveTop50Response {
    List<RetrieveTop50> retrieveTop50List;
}
