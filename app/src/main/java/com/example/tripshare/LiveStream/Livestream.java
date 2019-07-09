package com.example.tripshare.LiveStream;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Livestream implements Serializable {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("source_connection_information")
    @Expose
    private SourceConnectionInformation sourceConnectionInformation;

    @SerializedName("thumbnail_url")
    @Expose
    private String thumbnail_url;

    @SerializedName("response")
    @Expose
    private String response;

    public String getResponse() {
        return response;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public Livestream(String id, String name, SourceConnectionInformation sourceConnectionInformation) {
        super();
        this.id = id;
        this.name = name;
        this.sourceConnectionInformation = sourceConnectionInformation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SourceConnectionInformation getSourceConnectionInformation() {
        return sourceConnectionInformation;
    }

    public void setSourceConnectionInformation(SourceConnectionInformation sourceConnectionInformation) {
        this.sourceConnectionInformation = sourceConnectionInformation;
    }

}
