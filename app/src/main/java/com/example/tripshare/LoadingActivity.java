package com.example.tripshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.LoginRegister.SNSloginActivity;

public class LoadingActivity extends AppCompatActivity {
    private Thread splashThread;
    public static PrefConfig prefConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        prefConfig = new PrefConfig(this);

        LottieAnimationView animationView = findViewById(R.id.lottie);
        animationView.playAnimation();

        splashThread = new Thread() { //쓰레드 객체 생성
            @Override
            public void run() {
                try {
                    synchronized (this) { //메인액티비티가 락 걸림
                        // Wait given period of time or exit on touch
                        wait(3000); //서브스레드를 잠들게 함.
                    }
                } catch (InterruptedException ex) {
                }
                finish(); //로딩 화면 끝냄
                // Run next activity
                if (prefConfig.readLoginStatus()){ //로그인하고 로그아웃을 안한 상태면 메인 화면으로
                    Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(LoadingActivity.this, com.example.tripshare.LoginRegister.SNSloginActivity.class);
                    startActivity(intent);
                }
            }
        };
        splashThread.start(); //먼저 시작하고 run매소드 실행
    }


}
