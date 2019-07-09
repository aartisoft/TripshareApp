package com.example.tripshare.Data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Hourly {
    @SerializedName("weatherIconUrl")
    @Expose
    private List<WeatherIconUrl> weatherIconUrl = null;
    @SerializedName("precipMM")
    @Expose
    private String rainperhour;

    public String getRainperhour() {
        return rainperhour;
    }

    public void setRainperhour(String rainperhour) {
        this.rainperhour = rainperhour;
    }

    public List<WeatherIconUrl> getWeatherIconUrl() {
        return weatherIconUrl;
    }

    public void setWeatherIconUrl(List<WeatherIconUrl> weatherIconUrl) {
        this.weatherIconUrl = weatherIconUrl;
    }
}
