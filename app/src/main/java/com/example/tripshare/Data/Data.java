package com.example.tripshare.Data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {
    @SerializedName("weather")
    @Expose
    private List<Weather> weather = null;

    @SerializedName("time_zone")
    @Expose
    private List<com.example.tripshare.Data.Timezone> timezone = null;

    public List<Translation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }

    @SerializedName("translations")
    @Expose
    private List<Translation> translations = null;


    public List<com.example.tripshare.Data.Timezone> getTimezone() {
        return timezone;
    }

    public void setTimezone(List<Timezone> timezone) {
        this.timezone = timezone;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

}
