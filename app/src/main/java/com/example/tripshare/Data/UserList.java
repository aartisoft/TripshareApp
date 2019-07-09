package com.example.tripshare.Data;

import com.example.tripshare.LoginRegister.User;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class UserList {
    @SerializedName("friendlist")
    private ArrayList<User> friendlist;

    @SerializedName("response")
    private String response;

    @SerializedName("total")
    private String total;

    public String getTotal() {
        return total;
    }

    public String getResponse() {
        return response;
    }

    public ArrayList<User> getmyfriendlist() {
        return friendlist;
    }

    public void myfriendlist(ArrayList<User> myfriendlist) {
        this.friendlist = myfriendlist;
    }
}
