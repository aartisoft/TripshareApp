package com.example.tripshare.Data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Timezone {

    @SerializedName("localtime")
    @Expose
    private String localtime;

    @SerializedName("zone")
    @Expose
    private String zone;

    public String getLocaltime() {
        return localtime;
    }

    public void setLocaltime(String localtime) {
        this.localtime = localtime;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}
