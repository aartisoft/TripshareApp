package com.example.tripshare.TripTalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/*
핸드폰이 재부팅 될 때(핸드폰이 꺼졌다 켜지면)
핸드폰이 보내는 메세지를 받는 곳. (나 ~ 재부팅 됬어~)
받으면 메세지를 받을 내 서비스를 실행시킨다.


*/

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(intent.ACTION_BOOT_COMPLETED)){
        //안드로이드 8.0(오레오)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                context.startForegroundService(new Intent(context, TalkService.class));
                Log.d(TAG, "onReceive: ");
            }else {
                context.startService(new Intent(context, TalkService.class));
            }
        }
    }
}
