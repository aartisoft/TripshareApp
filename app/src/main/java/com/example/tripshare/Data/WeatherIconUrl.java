package com.example.tripshare.Data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WeatherIconUrl {
    @SerializedName("value")
    @Expose
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
