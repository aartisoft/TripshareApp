package com.example.tripshare.Data;

import com.example.tripshare.WhereWhen.Trip;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class TripList {
    @SerializedName("tripList")
    private ArrayList<TripData> tripDataArrayList;

    public ArrayList<TripData> getTripArrayList() {
        return tripDataArrayList;
    }

    public void setEmployeeArrayList(ArrayList<TripData> tripDataArrayList) {
        this.tripDataArrayList = tripDataArrayList;
    }
}
