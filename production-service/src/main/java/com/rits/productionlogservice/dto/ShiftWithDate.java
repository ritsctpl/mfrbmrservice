package com.rits.productionlogservice.dto;

import java.time.LocalDate;

public class ShiftWithDate {
    private LocalDate date;
    private Shift shift;

    public ShiftWithDate(LocalDate date, Shift shift) {
        this.date = date;
        this.shift = shift;
    }

    public LocalDate getDate() {
        return date;
    }

    public Shift getShift() {
        return shift;
    }

}
