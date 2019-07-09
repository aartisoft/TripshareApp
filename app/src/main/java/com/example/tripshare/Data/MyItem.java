package com.example.tripshare.Data;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {

    private LatLng mPosition;
    private String mTitle;
    private int totalrating;

    public MyItem(double lat, double lng) {

        mPosition = new LatLng(lat,lng);

    }

    public MyItem(double lat, double lng, String title, int total) {
        mPosition =new LatLng(lat,lng);
        mTitle = title;
        totalrating = total;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return "총 평가 수 : "+String.valueOf(totalrating);

    }
}
