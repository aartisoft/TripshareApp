package com.example.tripshare.LiveStream;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class StreamRoomList {
    @SerializedName("streamroomlist")
    private ArrayList<Livestream>  streamroomlist;

    @SerializedName("response")
    private String response;

    public ArrayList<Livestream> getStreamRoomlist() {

        return streamroomlist;
    }

    public String getResponse() {
        return response;
    }
}
