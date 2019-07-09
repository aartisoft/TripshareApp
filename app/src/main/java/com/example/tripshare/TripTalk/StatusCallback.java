package com.example.tripshare.TripTalk;

import android.util.Log;

import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

public class StatusCallback implements WOWZStatusCallback {
    private static final String TAG = "StatusCallback";
    @Override
    public void onWZStatus(WOWZStatus wowzStatus) {
        Log.d(TAG, "onWZStatus: "+wowzStatus.toString());
    }

    @Override
    public void onWZError(WOWZStatus wowzStatus) {
        Log.d(TAG, "onWZError: "+wowzStatus.toString());
    }
}
