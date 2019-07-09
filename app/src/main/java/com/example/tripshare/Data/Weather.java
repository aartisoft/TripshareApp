package com.example.tripshare.Data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("maxtempC")
    @Expose
    private String maxtempC;
    @SerializedName("mintempC")
    @Expose
    private String mintempC;
    @SerializedName("hourly")
    @Expose
    private List<Hourly> hourly = null;

    public Weather(String date, String maxtempC, String mintempC, List<Hourly> hourly) {
        this.date = date;
        this.maxtempC = maxtempC;
        this.mintempC = mintempC;
        this.hourly = hourly;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMaxtempC() {
        return maxtempC;
    }

    public void setMaxtempC(String maxtempC) {
        this.maxtempC = maxtempC;
    }

    public String getMintempC() {
        return mintempC;
    }

    public void setMintempC(String mintempC) {
        this.mintempC = mintempC;
    }


    public List<Hourly> getHourly() {
        return hourly;
    }

    public void setHourly(List<Hourly> hourly) {
        this.hourly = hourly;
    }

}
