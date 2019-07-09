package com.example.tripshare.Data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RoomList {

    @SerializedName("roomlist")
    private ArrayList<Room> roomlist;

    @SerializedName("response")
    private String response;

    public ArrayList<Room> getRoomlist() {
        return roomlist;
    }

    public String getResponse() {
        return response;
    }
}
