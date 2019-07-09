package com.example.tripshare.Data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PlaceList {
    @SerializedName("PlaceList")
    private ArrayList<OnedayPlace> placeArrayList;

    private String response;

    public String getResponse() {
        return response;
    }

    public ArrayList<OnedayPlace> getPlaceArrayList() {
        return placeArrayList;
    }

    public void setPlaceArrayList(ArrayList<OnedayPlace> placeArrayList) {
        this.placeArrayList = placeArrayList;
    }
}
