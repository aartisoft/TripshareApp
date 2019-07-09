package com.example.tripshare.Ar;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.tripshare.R;

import java.util.List;

public class ArActivity extends AppCompatActivity {

    ImageView dragonArImgV, movieArImgV, portalArImgV;
    boolean isExist;
    String dragon_project = "com.Ar.Dragon";
    String portal_project = "com.Ar.portalfourU";
    String video_project = "com.vuforia.engine.CoreSamplesUnity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        dragonArImgV = findViewById(R.id.ex_dragon_Ar);
        movieArImgV =findViewById(R.id.ar_movie_Ar);
        portalArImgV = findViewById(R.id.ar_portal_Ar);

        dragonArImgV.setOnClickListener(v -> {
            //앱이 설치되있는지 확인한다.
            getPackageList(dragon_project);
            //드래곤 앱으로 간다.
            StartOtherApps(dragon_project);

            //startActivity(new Intent(ArActivity.this, ArFirstActivity.class));
        });

        //영화 예고편 보는 ar로 간다.
        movieArImgV.setOnClickListener(v ->{
            //영화 예고편 앱이 설치되있는지 확인한다.
            getPackageList(video_project);
            //영화 예고편 앱으로 간다.
            StartOtherApps(video_project);
        });

        portalArImgV.setOnClickListener(v ->{
            //포탈앱이 설치되있는지 확인한다.
            getPackageList(portal_project);
            //포탈 앱으로 간다.
            StartOtherApps(portal_project);

        });
    }

    //설치 되있으면 해당 앱 실행
    //설치 안되있으면 플레이스토어에서 검색되게 한다.
    private void StartOtherApps(String project) {
        if (isExist) {
            Intent intent = getPackageManager().getLaunchIntentForPackage(project);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else{
            String url = "market://details?id=" + project;
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
            }
    }

    //해당 앱이 기기에 설치 되있는지 확인하기
    public boolean getPackageList(String project) {
        isExist = false;

        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith(project)){
                    isExist = true;
                    break;
                }
            }
        }
        catch (Exception e) {
            isExist = false;
        }
        return isExist;
    }
}
