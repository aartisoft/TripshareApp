package com.example.tripshare.Data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ResultsList {

    @SerializedName("ResultsList")
    private ArrayList<Results> ResultsList;

    private String[] response;

    public String[] getResponse() {
        return response;
    }

    public ArrayList<Results> getResultsList() {
        return ResultsList;
    }

    public void setResultsList(ArrayList<Results> resultsList) {
        this.ResultsList = resultsList;
    }
}
