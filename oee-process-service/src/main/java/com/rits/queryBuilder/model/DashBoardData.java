package com.rits.queryBuilder.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashBoardData {
    private String category;
    private List<Data> data;
    private boolean enabled;
    public DashBoardData(DashBoardData original) {
        if (original != null) {
            this.category = original.category;
            if (original.data != null) {
                this.data = new ArrayList<>();
                for (Data d : original.data) {
                    this.data.add(new Data(d));  // Deep copy Data
                }
            }
        }
    }
}
