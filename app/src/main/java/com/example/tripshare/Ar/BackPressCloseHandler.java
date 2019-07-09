package com.example.tripshare.Ar;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class BackPressCloseHandler {
    private long backKeyClickTime = 0;
    private Activity activity;
    private static final String TAG = "back";

    public BackPressCloseHandler(Activity activity) {
        Log.d(TAG, "BackPressCloseHandler: ");
        this.activity = activity;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyClickTime + 2000) {
            backKeyClickTime = System.currentTimeMillis();
            Log.d(TAG, "onBackPressed:  "+backKeyClickTime);
            showToast();
            return;
        }
        if (System.currentTimeMillis() <= backKeyClickTime + 2000) {
            activity.finish();
        }
    }

    public void showToast() {
        Toast.makeText(activity, "뒤로 가기 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
    }

}
