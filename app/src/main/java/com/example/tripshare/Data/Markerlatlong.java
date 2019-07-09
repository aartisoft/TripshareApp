package com.example.tripshare.Data;

public class Markerlatlong {
    double latitude;
    double longitude;
    private String name;

    public Markerlatlong(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
