package com.rits.loggingservice.model;

import lombok.*;
//import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity(name = "log_entries")
//@Document(collection = "R_LOG_TRACES")
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String level;
    private String message;
    private String userId;
    private String location;
    private String application;
    private String date;
    private String time;
    // Optionally, you can use a Date or Instant type

    // Constructors

}
