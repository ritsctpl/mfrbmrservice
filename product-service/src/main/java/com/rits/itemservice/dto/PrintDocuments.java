package com.rits.itemservice.dto;

import com.rits.itemservice.model.PrintDocument;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PrintDocuments {
    private List<PrintDocument> printDocuments;
}
