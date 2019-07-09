package com.example.tripshare.Fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.tripshare.MainActivity;
import com.example.tripshare.R;

public class MyNotificationManager {
    private static final String TAG = "fcMMyNotificationMan";
    private Context ctx;
    public static final int NOTIFICATION_ID= 234;
    private static MyNotificationManager mInstance;

    public MyNotificationManager(Context context){
        this.ctx =context;
        Log.d(TAG, "MyNotificationManager:context "+context);
    }

    public static synchronized MyNotificationManager getInstance(Context context){
        if (mInstance ==null){
            mInstance = new MyNotificationManager(context);
            Log.d(TAG, "getInstance: ");
        }
        return mInstance;
    }

    public void displayNotification(String message,String tnum,String fromemail){
        //알람 설정
        NotificationCompat.Builder mbuilder = new NotificationCompat.Builder(ctx, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("나랑 같이 갈래요?")
                .setTicker("같이 여행 가요!")//상태바에 표시될 한 줄 출력
                .setAutoCancel(true)
                .setContentText(message);
        Log.d(TAG, "displayNotification: "+message);
        Intent intent = new Intent(ctx, MainActivity.class);
        //클릭할 때까지 인텐트 행위(액티비티 이동)를 보류함
        //이미 생성된 pendingintent의 해당 extra data만 변경함

        //메인을 클릭할 시 보낼 데이터들
        intent.putExtra("invite", tnum);
        intent.putExtra("message",message);
        intent.putExtra("fromemail",fromemail);
        //기존에 엑티비티가 있다면 없에줌
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        mbuilder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(TAG, "displayNotification: ");
        if (mNotificationManager != null){
            Log.d(TAG, "displayNotification: ");
            mNotificationManager.notify(1,mbuilder.build());
        }
    }
}
