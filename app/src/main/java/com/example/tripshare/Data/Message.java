package com.example.tripshare.Data;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Message implements Serializable {
    @SerializedName("sender")
    private String senderemail;
    @SerializedName("image")
    private String imgurl;
    @SerializedName("message")
    private String message;
    @SerializedName("rnum")
    private String rnum;
    private String response;
    @SerializedName("sendername")
    private String sendername;

    private Uri photouri;


    public Message(String senderemail, String imgurl, String message, String sendername, String type) {
        this.senderemail = senderemail;
        this.imgurl = imgurl;
        this.message = message;
        this.sendername = sendername;
        this.type = type;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRnum(String rnum) {
        this.rnum = rnum;
    }

    public void setSendername(String sendername) {
        this.sendername = sendername;
    }

    public void setPhotouri(Uri photouri) {
        this.photouri = photouri;
    }

    public void setYmd(String ymd) {
        this.ymd = ymd;
    }

    public void setHm(String hm) {
        this.hm = hm;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }


    @SerializedName("ymd")
    private String ymd;

    @SerializedName("hm")
    private String hm;

    @SerializedName("type")
    private String type;

    private String bitmaptoString;
    private String total;

    public void setBitmaptoString(String bitmaptoString) {
        this.bitmaptoString = bitmaptoString;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getBitmaptoString() {
        return bitmaptoString;
    }

    private byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }

    public Uri getPhotouri() {
        return photouri;
    }

    public String getType() {
        return type;
    }

    public Message(String type, String senderemail, String imgurl, String message, String rnum, String sendername, String ymd, String hm) {
        this.type = type;
        this.senderemail = senderemail;
        this.imgurl = imgurl;
        this.message = message;
        this.rnum = rnum;
        this.sendername = sendername;
        this.ymd = ymd;
        this.hm = hm;
    }


    public Message(String type, String senderemail, String imgurl, String bitmap,String message, String rnum, String sendername, String ymd, String hm) {
        this.type = type;
        this.senderemail = senderemail;
        this.imgurl = imgurl;
        this.bitmaptoString = bitmap;
        this.message = message;
        this.rnum = rnum;
        this.sendername = sendername;
        this.ymd = ymd;
        this.hm = hm;
    }

    public void setSenderemail(String senderemail) {
        this.senderemail = senderemail;
    }

    public String getSenderemail() {
        return senderemail;
    }

    public String getImgurl() {
        return imgurl;
    }

    public String getMessage() {
        return message;
    }

    public String getRnum() {
        return rnum;
    }

    public String getResponse() {
        return response;
    }

    public String getSendername() {
        return sendername;
    }

    public String getYmd() {
        return ymd;
    }

    public String getHm() {
        return hm;
    }
}