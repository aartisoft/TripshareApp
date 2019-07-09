package com.example.tripshare.LoginRegister;

import android.app.Activity;
import android.app.Application;

import com.kakao.auth.KakaoSDK;

/*
kakao sdk를 내 앱에 사용하는게 목표
1. sdk를 내 앱에 연결해주는 adater를 작성하고 초기화해야함
2. adater를 초기화 하는 과정

*/

public class GlobalApplication extends Application {


    private static volatile GlobalApplication obj = null;
    private static volatile Activity currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        obj = this;
        KakaoSDK.init(new KaKaoSDKAdapter());
    }

    public static GlobalApplication getGlobalApplicationContext() {
        return obj;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    // Activity가 올라올때마다 Activity의 onCreate에서 호출해줘야한다.
    public static void setCurrentActivity(Activity currentActivity) {
        GlobalApplication.currentActivity = currentActivity;
    }

}