package com.example.tripshare;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*
retrofit 객체를 만드는 곳이다.
retrofit 객체는
데이터를 주고받을 서버 url을 사용하고
gson라이브러리를 통해 서버에게 받은 json데이터를 처리한다.

*/

public class ApiClient {
    //baseurl
    //public static final String BASE_URL ="http://10.0.2.2/loginapp/";

    public static final String BASE_URL ="http://115.71.238.81/";
    public static Retrofit retrofit = null;
    private static final String TAG = "ApiClient";
    public static Retrofit getApiClient(){
        if (retrofit ==null){
//            Gson gson = new GsonBuilder()
//                    .setLenient()
//                    .create();
//
            Log.d(TAG, "getApiClient: null이 아니야");
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
