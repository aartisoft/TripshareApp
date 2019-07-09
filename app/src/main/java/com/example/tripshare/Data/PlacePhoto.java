package com.example.tripshare.Data;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.android.libraries.places.api.model.PhotoMetadata;

@SuppressLint("ParcelCreator")
public  class PlacePhoto extends PhotoMetadata {
    private int height;
    private int width;
    private String html_attributions;
    private String photo_reference;

    public PlacePhoto(int height, int width, String html_attributions, String photo_reference) {
        this.height = height;
        this.width = width;
        this.html_attributions = html_attributions;
        this.photo_reference = photo_reference;
    }

    @NonNull
    @Override
    public String getAttributions() {
        return null;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @NonNull
    @Override
    public String a() {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
