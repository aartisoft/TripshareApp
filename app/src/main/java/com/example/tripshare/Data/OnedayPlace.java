package com.example.tripshare.Data;

public class OnedayPlace {
    private String placename;
    private String placeid;
    private String response;

    private int tnum;
    private int date;
    private int porder;
    private int numorder;

    private double latitude;
    private double longitude;

    public OnedayPlace(String placename, String placeid, int tnum, int date, int porder, int numorder, double latitude, double longitude) {
        this.placename = placename;
        this.placeid = placeid;
        this.tnum = tnum;
        this.date = date;
        this.porder = porder;
        this.numorder = numorder;
        this.latitude = latitude;
        this.longitude = longitude;
    }



    public int getNumorder() {
        return numorder;
    }

    public String getResponse() {
        return response;
    }
    public String getPlacename() {
        return placename;
    }

    public void setPlacename(String placename) {
        this.placename = placename;
    }

    public String getPlaceid() {
        return placeid;
    }

    public void setPlaceid(String placeid) {
        this.placeid = placeid;
    }

    public int getTnum() {
        return tnum;
    }

    public void setTnum(int tnum) {
        this.tnum = tnum;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getPorder() {
        return porder;
    }

    public void setPorder(int porder) {
        this.porder = porder;
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
}
