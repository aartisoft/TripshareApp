package com.example.tripshare.WhereWhen;

public class Trip {

    private String locationid;
    private String tstart;
    private String tend;
    private String howlong;
    private String email;
    private String response;
    private String planresponse;
    private String countrycode;
    private int tnum;
    private double latitude,longitude;


    public String getCountrycode() {
        return countrycode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPlanresponse() {
        return planresponse;
    }

    public int getTnum() {
        return tnum;
    }

    public String getResponse() {
        return response;
    }

    public String getLocationid() {
        return locationid;
    }

    public String getTstart() {
        return tstart;
    }

    public String getTend() {
        return tend;
    }

    public String getHowlong() {
        return howlong;
    }

    public String getEmail() {
        return email;
    }
}
