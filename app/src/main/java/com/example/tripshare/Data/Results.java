package com.example.tripshare.Data;


import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Results implements Serializable , ClusterItem {
    private LatLng latLng;

    private double latitude;
    private double longitude;
    private String name;
    private double rating;
    private int user_ratings_total;
//    private PlacePhoto placePhoto;

    private String category;
    private String url;

    public Results(LatLng latLng) {
        this.latLng = latLng;
    }

    public Results(double latitude, double longitude, String name, double rating, int user_ratings_total, String category, String url) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.rating = rating;
        this.user_ratings_total = user_ratings_total;
        this.category = category;
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public String getUrl() {
        return url;
    }

    public Results(double latitude, double longitude, String name, double rating, int user_ratings_total) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.rating = rating;
        this.user_ratings_total = user_ratings_total;
       // this.placePhoto = placePhoto;
    }

//    public PlacePhoto getPlacePhoto() {
//        return placePhoto;
//    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public int getUser_ratings_total() {
        return user_ratings_total;
    }

    @Override
    public LatLng getPosition() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
