package com.rits.licencevalidationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "R_LICENCE")
public class License {

    @Id
    private String id;
    private String userId;
    private String licenseKey;
    private Date validFrom;
    private Date validTo;
    private boolean isActive;
}
