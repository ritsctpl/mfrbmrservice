package com.rits.uomservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "R_UOM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UOMEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String uomCode;

    private String description;


    private Double conversionFactor;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String site;

    @Column(nullable = false)
    private Integer active;

    @CreationTimestamp
    private LocalDateTime createdDateTime;

    @UpdateTimestamp
    private LocalDateTime modifiedDateTime;
}
