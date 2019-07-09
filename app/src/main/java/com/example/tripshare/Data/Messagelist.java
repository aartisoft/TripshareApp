package com.example.tripshare.Data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Messagelist {
    @SerializedName("messagelist")
    ArrayList<Message> messageslist;

    String response;

    public ArrayList<Message> getMessageslist() {
        return messageslist;
    }

    public String getResponse() {
        return response;
    }
}
