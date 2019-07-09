package com.example.tripshare.Data;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Room implements Comparable<Room> {
    @SerializedName("rnum")
    private String rnum;
    @SerializedName("name")
    private String yourname;
    @SerializedName("image")
    private String yourimgurl;

    @SerializedName("email")
    private String youremail;
    private String lastmessage;
    @SerializedName("ymd")
    private String ymd;
    @SerializedName("hm")
    private String hm;

    @SerializedName("total")
    private String total;

    @SerializedName("type")
    private String type;

    public void setLastmessage(String lastmessage) {
        this.lastmessage = lastmessage;
    }

    public void setYmd(String ymd) {
        this.ymd = ymd;
    }

    public void setHm(String hm) {
        this.hm = hm;
    }

    private Date datetime;

    private String response;

    public String getType() {
        return type;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }


    public String getTotal() {
        return total;
    }


    public String getResponse() {
        return response;
    }

    public String getYmd() {
        return ymd;
    }

    public String getHm() {
        return hm;
    }

    public String getLastmessage() {
        return lastmessage;
    }

    public String getRnum() {
        return rnum;
    }

    public String getYourname() {
        return yourname;
    }

    public String getYourimgurl() {
        return yourimgurl;
    }

    public String getYouremail() {
        return youremail;
    }

    @Override
    public int compareTo(Room o) {
        if (getDatetime() == null || o.getDatetime() == null)
            return 0;
        return getDatetime().compareTo(o.getDatetime());
    }
}
