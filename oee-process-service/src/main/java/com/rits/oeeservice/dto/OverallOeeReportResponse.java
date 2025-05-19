package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OverallOeeReportResponse {
    private String site;  // The site for which the report is generated
    private CellGroupData cellGroups;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CellGroupData {
        private String cellGroupCategory; // Cell Group Category Name
        private double oee;               // OEE for the cell group
        private double availability;      // Availability for the cell group
        private double quality;           // Quality for the cell group
        private double performance;       // Performance for the cell group
        private double targetQty;
        private double actualQty;
        private List<CellData> cells;    // List of cells in the cell group
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CellData {
        private String cell;              // Cell name
        private double oee;               // OEE for the cell
        private double availability;      // Availability for the cell
        private double quality;           // Quality for the cell
        private double performance;       // Performance for the cell
        private double targetQty;
        private double actualQty;
        private List<LineData> lines;     // List of lines (workcenters) for the cell
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineData {
        private String line;              // Line name (same as workcenter)
        private double oee;               // OEE for the line (workcenter)
        private double availability;      // Availability for the line (workcenter)
        private double quality;           // Quality for the line (workcenter)
        private double performance;       // Performance for the line (workcenter)
        private double targetQty;
        private double actualQty;
    }
}
