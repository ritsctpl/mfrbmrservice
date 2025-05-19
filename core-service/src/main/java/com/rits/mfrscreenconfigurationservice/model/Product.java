package com.rits.mfrscreenconfigurationservice.model;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class Product {

    private Header header;

    private List<Sections> sections;
    private Footer footer;
}
