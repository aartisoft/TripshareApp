package com.example.tripshare.Fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "fcMMyFirebaseMessaging";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
       //fcm메세지를 받았을 때 호출됨

        // TODO(developer): Handle FCM messages here.

        //메세지 받는 형태가 배열이라 배열의 key를 입력하면 됨
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "onMessageReceived: "+remoteMessage.getData().get("message"));

        String message = remoteMessage.getData().get("message");
        String tnum =remoteMessage.getData().get("tnum");
        Log.d(TAG, "onMessageReceived: "+remoteMessage.getData().get("tnum"));
        String fromemail =remoteMessage.getData().get("fromemail");
        //유저에게 메세지를 보낸다. 메세지랑 해당 여행번호,누가 보냈는지
        MyNotificationManager.getInstance(getApplicationContext()).displayNotification(message,tnum,fromemail);
    }

}
