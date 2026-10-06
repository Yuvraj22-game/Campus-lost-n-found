package com.example.campuslost_and_found.data;

/**
 * Data class representing count of items grouped by Campus Zone.
 */
public class ZoneCount {
    private String zone;
    private int count;

    public ZoneCount(String zone, int count) {
        this.zone = zone;
        this.count = count;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
