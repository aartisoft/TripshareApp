package com.example.tripshare.Fcm;

import android.content.Intent;
import android.util.Log;

import com.example.tripshare.LoginRegister.PrefConfig;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = "fcMMyFirebaseInstanceIr";
    public static final String TOKEN_BROADCAST = "myfcmtokenbroadcast";
    //토큰이 새로 생성될 때마다 호출되서 새로운 토큰 줌
    public static PrefConfig prefConfig;
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefresh: "+refreshedToken);
        getApplicationContext().sendBroadcast(new Intent(TOKEN_BROADCAST));
        Log.d(TAG, "onTokenRefresh:storeToken ");
        storeToken(refreshedToken);
    }

    private void storeToken(String refreshedToken) {
        prefConfig = new PrefConfig(getApplicationContext());
        Log.d(TAG, "storeToken: "+refreshedToken);
        prefConfig.storeToken(refreshedToken);
    }
}
