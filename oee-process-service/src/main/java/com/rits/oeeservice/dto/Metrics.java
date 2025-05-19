package com.rits.oeeservice.dto;

public class Metrics {
    private long totalTimeSeconds;
    private long productionTimeSeconds;
    private long actualTimeSeconds;
    private double totalDowntime;
    private double plannedDowntime;
    private double totalGoodQty;
    private double totalBadQty;
    private double totalPlannedQty;
    private double availability;
    private double performance;
    private double quality;
    private double oee;
    private double plannedCycleTime;
    private double actualCycleTime;

    // Constructor
    public Metrics(long totalTimeSeconds, long productionTimeSeconds, double totalDowntime,
                   double totalGoodQty, double totalBadQty, double totalPlannedQty,
                   double availability, double performance, double quality, double oee,
                   double actualCycleTime, double plannedCycleTime,long actualTimeSeconds,double plannedDowntime) {
        this.totalTimeSeconds = totalTimeSeconds;
        this.productionTimeSeconds = productionTimeSeconds;

        this.totalDowntime = totalDowntime;
        this.totalGoodQty = totalGoodQty;
        this.totalBadQty = totalBadQty;
        this.totalPlannedQty = totalPlannedQty;
        this.availability = availability;
        this.performance = performance;
        this.quality = quality;
        this.oee = oee;
        this.actualCycleTime = actualCycleTime;
        this.plannedCycleTime = plannedCycleTime;
        this.actualTimeSeconds = actualTimeSeconds;
        this.plannedDowntime = plannedDowntime;


    }

    // Getters and Setters
    public long getTotalTimeSeconds() {
        return totalTimeSeconds;
    }
    public double getPlannedDowntime() {
        return plannedDowntime;
    }
    public void setPlannedDowntime(double plannedDowntime) {
        this.plannedDowntime = plannedDowntime;
    }

    public void setTotalTimeSeconds(long totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public long getProductionTimeSeconds() {
        return productionTimeSeconds;
    }

    public void setProductionTimeSeconds(long productionTimeSeconds) {
        this.productionTimeSeconds = productionTimeSeconds;
    }
    public long getActualTimeSeconds() {
        return actualTimeSeconds;
    }

    public double getTotalDowntime() {
        return totalDowntime;
    }

    public void setTotalDowntime(double totalDowntime) {
        this.totalDowntime = totalDowntime;
    }

    public double getTotalGoodQty() {
        return totalGoodQty;
    }

    public void setTotalGoodQty(double totalGoodQty) {
        this.totalGoodQty = totalGoodQty;
    }

    public double getTotalBadQty() {
        return totalBadQty;
    }

    public void setTotalBadQty(double totalBadQty) {
        this.totalBadQty = totalBadQty;
    }

    public double getTotalPlannedQty() {
        return totalPlannedQty;
    }

    public void setTotalPlannedQty(double totalPlannedQty) {
        this.totalPlannedQty = totalPlannedQty;
    }

    public double getAvailability() {
        return availability;
    }

    public void setAvailability(double availability) {
        this.availability = availability;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public double getOee() {
        return oee;
    }

    public void setOee(double oee) {
        this.oee = oee;
    }

    public double getPlannedCycleTime() {
        return plannedCycleTime;
    }

    public void setPlannedCycleTime(double plannedCycleTime) {
        this.plannedCycleTime = plannedCycleTime;
    }

    public double getActualCycleTime() {
        return actualCycleTime;
    }

    public void setActualCycleTime(double actualCycleTime) {
        this.actualCycleTime = actualCycleTime;
    }
    public void setActualTimeSeconds(long actualTimeSeconds) {
        this.actualTimeSeconds = actualTimeSeconds;
    }

    @Override
    public String toString() {
        return "Metrics{" +
                "totalTimeSeconds=" + totalTimeSeconds +
                ", productionTimeSeconds=" + productionTimeSeconds +
                ", totalDowntime=" + totalDowntime +
                ", totalGoodQty=" + totalGoodQty +
                ", totalBadQty=" + totalBadQty +
                ", totalPlannedQty=" + totalPlannedQty +
                ", availability=" + availability +
                ", performance=" + performance +
                ", quality=" + quality +
                ", oee=" + oee +
                ", actualCycleTime=" + actualCycleTime +
                ", plannedCycleTime=" + plannedCycleTime +
                ", actualTimeSeconds=" + actualTimeSeconds +
                ", plannedDowntime=" + plannedDowntime +
                '}';
    }
}
