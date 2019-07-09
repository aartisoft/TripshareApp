package com.example.tripshare.TripTalk;


import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telecom.Call;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.R;
import com.example.tripshare.androidwebrtc.call.CallActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.example.tripshare.androidwebrtc.util.Constants.EXTRA_ROOMID;


public class VideoCallActivity extends AppCompatActivity {
    private static final String TAG = "VideoCallActivity";
    private String senderurl, sendername, senderemail, rnum;
    private CircleImageView circleImageView;
    private ImageButton cancelbt, acceptbt;
    private TextView nametx;
    private static final int CONNECTION_REQUEST = 1;
    private static final int RC_CALL = 111;
    String videoroomstr, ymd, hm, myemail, myname, myurl;
    private boolean mBound;
    private Messenger mMessenger, mService;

    private PrefConfig prefConfig;

    class endvideocallHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TalkService.MSG_SET_RNUM:
                    Toast.makeText(VideoCallActivity.this, "상대방이 통화를 종료했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            mMessenger = new Messenger(new endvideocallHandler());
            Log.d(TAG, "onServiceConnected: " + className);
            //service에서 받은 IBinsder를 가지고 messenger 객체 생성
            //messenger객체는 service로 데이터를 보낸다. 즉 서비스와 통신한다.
            mService = new Messenger(service);
            mBound = true;
            Log.d(TAG, "onServiceConnected:bound " + mBound);
            try {
                //보낼 메세지를 생성한다.
                android.os.Message msg = android.os.Message.obtain(null, TalkService.MSG_REGISTER_CLIENT);
                //서비스가 보낸 메세지를 받기 위한 messanger객체를 보낼 message에 넣는다.
                msg.replyTo = mMessenger;
                //IBinder를 가진 messenger객체를 통해 메세지를 보낸다.
                mService.send(msg);

                String room = "givemenoti";
                //방 번호를 서비스에게 보낸다. 나중에 메세지가 왔을 때 메세지의 방번호가 일치하면 메세지를 준다.
                Log.d(TAG, "onServiceConnected: set " + TalkService.MSG_SET_RNUM);
                msg = android.os.Message.obtain(null, TalkService.MSG_SET_RNUM, room);
                mService.send(msg);
            } catch (RemoteException e) {
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: " + name);
            mService = null;
            mBound = false;
            Log.d(TAG, "onServiceDisconnected:bound " + mBound);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        // Bind to the service
        bindService(new Intent(this, TalkService.class), mConnection,
                Context.BIND_AUTO_CREATE);


        prefConfig = new PrefConfig(this);
        myemail = prefConfig.readEmail();
        myname = prefConfig.getName();
        myurl = prefConfig.readimgurl();

        senderemail = getIntent().getStringExtra("senderemail");
        sendername = getIntent().getStringExtra("sendername");
        senderurl = getIntent().getStringExtra("senderurl");
        rnum = getIntent().getStringExtra("rnum");

        nametx = findViewById(R.id.name_tx_video);
        circleImageView = findViewById(R.id.cirimg_your);
        cancelbt = findViewById(R.id.cancel_imgbt);
        acceptbt = findViewById(R.id.accept_imgbt);

        nametx.setText(sendername);

        Glide.with(this).load(senderurl).into(circleImageView);

        cancelbt.setOnClickListener(v -> {
            //거절 시간,날짜
            getymdhm();
            //거절 했다는 메세지를 상대방에게 보내야 한다.
            new Declinerequest().execute();

        });

        acceptbt.setOnClickListener(v -> {
            //권한 요청
            connect();
        });
        Log.d(TAG, "onCreate: "+senderurl);
        Log.d(TAG, "onCreate: "+sendername);
        Log.d(TAG, "onCreate: "+senderemail);
        Log.d(TAG, "onCreate: "+rnum);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause:bound " + mBound);
        try {
            String notchatroom = "givemenoti";
            android.os.Message message = android.os.Message.obtain(null, TalkService.MSG_SET_RNUM, notchatroom);
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (mBound) {
            Log.d(TAG, "onStop:unbind ");
            //채팅방이 아닌 다른 화면이나 앱 바깥에서 알람을 받을 수 있게 서비스에게 채팅방이 아니라고 알려준다.
            unbindService(mConnection);
            mBound = false;
            Log.d(TAG, "onStop:bound " + mBound);
        }
        //imm.showSoftInput(messageedit, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        Log.d(TAG, "onStop: ");

    }


    //권한 요청 결과를 확인하는 곳
    //권한 요청 이후에 호출된다.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        Log.d(TAG, "onRequestPermissionsResult: ");
    }
    private void getymdhm() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy년 M월 d일 E-a K시 m분");
        Date date = new Date();
        String today = df.format(date);
        String[] todayarray = today.split("-");
        ymd = todayarray[0];
        hm = todayarray[1];
        Log.d(TAG, "onCreate: " + ymd + "시간" + hm);
    }

    //권한 승인 뒤에
    @AfterPermissionGranted(RC_CALL)
    private void connect() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            connectToRoom(rnum);
        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
    }
    //roomid들 키로하고 사용자가 입력한 방 번호를 가지고
    //영상통화 화면으로 이동한다.
    private void connectToRoom(String roomId) {
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(EXTRA_ROOMID, roomId);
        intent.putExtra("youremail",senderemail);
        startActivityForResult(intent, CONNECTION_REQUEST);
        finish();
    }
    private class Declinerequest extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: hi i'm item_message_sender");

            //사용자 각각 알림에 나를 제외한 사용자를 넣기 위해
            String type ="mtext";
            String total ="1";
            String receiveremail = senderemail;
            String message = "declinevideocall";


            try {
                Log.d(TAG, "doInBackground: type " + type);
                Log.d(TAG, "doInBackground: total " + total);
                Log.d(TAG, "doInBackground: "+myemail);
                Log.d(TAG, "doInBackground: total " + receiveremail);
                Log.d(TAG, "doInBackground: total " + message);
                Log.d(TAG, "doInBackground: total " + rnum);
                Log.d(TAG, "doInBackground: total " + myname);
                Log.d(TAG, "doInBackground: total " + myurl);
                Log.d(TAG, "doInBackground: total " + ymd);
                Log.d(TAG, "doInBackground: total " + hm);

                TalkService.out.writeUTF(type);
                TalkService.out.writeUTF(total);
                TalkService.out.writeUTF(myemail);
                TalkService.out.writeUTF(receiveremail);
                TalkService.out.writeUTF(rnum);
                TalkService.out.writeUTF(myname);
                TalkService.out.writeUTF(myurl);
                TalkService.out.writeUTF(ymd);
                TalkService.out.writeUTF(hm);
                TalkService.out.writeUTF(message);


            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground:error " + e.getMessage());
            }
            Log.d(TAG, "doInBackground: end");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }
}