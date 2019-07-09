package com.example.tripshare.Data;

import com.google.gson.annotations.SerializedName;

public class TripData {
    private static final String TAG = "TripData";
    @SerializedName("placename")
    private String placename;
    @SerializedName("tstart")
    private String startdate;
    @SerializedName("tend")
    private String enddate;
    @SerializedName("howlong")
    private int term;
    @SerializedName("locationid")
    private String placeid;
    @SerializedName("tnum")
    private int tnum;
    @SerializedName("countrycode")
    String countrycode;



    String response;
    private double latitude,longitude;

    public TripData(String placename, String startdate, String enddate, int term, String placeid, int tnum, double latitude, double longitude, String countrycode) {
        this.placename = placename;
        this.startdate = startdate;
        this.enddate = enddate;
        this.term = term;
        this.placeid = placeid;
        this.tnum = tnum;
        this.latitude = latitude;
        this.longitude = longitude;
        this.countrycode = countrycode;
    }
    public String getResponse() {
        return response;
    }
    public String getCountrycode() {
        return countrycode;
    }

    public String getPlaceid() {
        return placeid;
    }

    public int getTnum() {
        return tnum;
    }

    public void setTnum(int tnum) {
        this.tnum = tnum;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setPlaceid(String placeid) {
        this.placeid = placeid;
    }

    public String getPlacename() {
        return placename;
    }

    public void setPlacename(String placename) {
        this.placename = placename;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }
}
