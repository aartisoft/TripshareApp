package com.example.tripshare.LiveStream;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SourceConnectionInformation implements Serializable {
    @SerializedName("primary_server")
    @Expose
    private String primaryServer;
    @SerializedName("application")
    @Expose
    private String application;
    @SerializedName("stream_name")
    @Expose
    private String streamName;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("walletaddress")
    @Expose
    private String walletaddress;


    public String getWalletaddress() {
        return walletaddress;
    }

    public SourceConnectionInformation(String primaryServer, String application, String streamName, String username, String password) {
        this.primaryServer = primaryServer;
        this.application = application;
        this.streamName = streamName;
        this.username = username;
        this.password = password;
    }

    public String getPrimaryServer() {
        return primaryServer;
    }

    public void setPrimaryServer(String primaryServer) {
        this.primaryServer = primaryServer;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
