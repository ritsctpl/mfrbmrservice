package com.rits.managementservice.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Data {

    private String handleRef;
    private String dataName;
    private String type;
    private String query;
    private String endPoint;
    private boolean enabled;
    private String seconds;
    private String column;
    private ColorSchemeItem colorSchemeItem;
    private ColorScheme colorScheme;
    public Data(Data original) {
        if (original != null) {
            this.dataName = original.dataName;
            this.handleRef = original.handleRef;
            this.type = original.type;
            this.query = original.query;
            this.endPoint = original.endPoint;
            this.enabled = original.enabled;
            this.seconds = original.seconds;
            this.column = original.column;
            // Create a deep copy of colorScheme if it's not null
            if (original.colorScheme != null) {
                this.colorScheme = new ColorScheme(original.colorScheme);
            }
        }
    }
}
