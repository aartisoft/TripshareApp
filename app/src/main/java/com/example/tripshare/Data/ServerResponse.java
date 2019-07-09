package com.example.tripshare.Data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ServerResponse {

    @SerializedName("success")
    boolean success;
    @SerializedName("message")
    String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
