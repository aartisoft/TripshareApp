/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.example.tripshare.androidwebrtc.main;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.tripshare.R;
import com.example.tripshare.androidwebrtc.call.CallActivity;
import com.example.tripshare.databinding.ActivityRoomBinding;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.example.tripshare.androidwebrtc.util.Constants.EXTRA_ROOMID;

/**
 * Handles the initial setup where the user selects which room to join.
 */
public class AppRTCMainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "AppRTCMainActivity";
    private static final int CONNECTION_REQUEST = 1;
    private static final int RC_CALL = 111;
    private ActivityRoomBinding binding;
    private static final String TAG = "AppRTCMainActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //설정화면을 해주는 것 같은데 어떻게 실재로 사용하는지 모르겠다.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //findviewbyid 해주는 bind
        binding = DataBindingUtil.setContentView(this, R.layout.activity_room);
        binding.connectButton.setOnClickListener(v -> connect());
        //방번호에 포커즈가게 한다.
        binding.roomEdittext.requestFocus();
    }


    //권한 요청 결과를 확인하는 곳
    //권한 요청 이후에 호출된다.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        Log.d(TAG, "onRequestPermissionsResult: ");
    }

    //권한 승인 뒤에
    @AfterPermissionGranted(RC_CALL)
    private void connect() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            connectToRoom(binding.roomEdittext.getText().toString());
        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
    }
    //roomid들 키로하고 사용자가 입력한 방 번호를 가지고
    //영상통화 화면으로 이동한다.
    private void connectToRoom(String roomId) {
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(EXTRA_ROOMID, roomId);
        startActivityForResult(intent, CONNECTION_REQUEST);
    }
}
