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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.Room;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.LoginRegister.User;
import com.example.tripshare.R;
import com.example.tripshare.androidwebrtc.call.CallActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.tripshare.androidwebrtc.util.Constants.EXTRA_ROOMID;

public class ProfileActivity extends AppCompatActivity {
    private static final int CONNECTION_REQUEST = 1;
    private static final int RC_CALL = 111;
    private TextView nametx;
    private CircleImageView imgcircle;
    private Button deletebt, meandyoubt, facetofacebt;
    private String yourname, youremail, myemail, yourimgurl, myurl,myname;
    public static PrefConfig prefConfig;
    public static ApiInterface apiInterface;
    private Context mctx;
    private static final String TAG = "ProfileActivity";
    String videoroomstr, ymd, hm;
    private boolean mBound;
    private Messenger mMessenger, mService;

    class gotovideocallHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TalkService.MSG_SET_RNUM:

                    break;
                case TalkService.IMG_SET_RNUM:
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

            mMessenger = new Messenger(new gotovideocallHandler());
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Bind to the service
        bindService(new Intent(this, TalkService.class), mConnection,
                Context.BIND_AUTO_CREATE);

        //설정화면을 해주는 것 같은데 어떻게 실재로 사용하는지 모르겠다.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


        mctx = getApplicationContext();
        //내 이메일 얻기, 서버로 친구 차단을 위한 통신
        prefConfig = new PrefConfig(mctx);
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        //뷰들 찾아주기
        deletebt = findViewById(R.id.delete_bt_profile);
        meandyoubt = findViewById(R.id.meandyou_bt_profile);
        facetofacebt = findViewById(R.id.facetoface_bt_profile);
        imgcircle = findViewById(R.id.cirimg_profile);
        nametx = findViewById(R.id.name_tx_profile);

        // 친구 이름,이메일,이미지 받기 (인텐트)
        yourname = getIntent().getStringExtra("name");
        youremail = getIntent().getStringExtra("email");
        yourimgurl = getIntent().getStringExtra("imgurl");

        Log.d(TAG, "onCreate: yourimg "+yourimgurl);
        //친구 이름이랑 이미지 사용자에게 보여주기
        Glide.with(mctx).load(yourimgurl).into(imgcircle);
        nametx.setText(yourname);

        //내 이메일
        myemail = prefConfig.readEmail();
        myname = prefConfig.getName();
        myurl = prefConfig.readimgurl();

        // 영상 통화 눌렀을 경우
        facetofacebt.setOnClickListener(v -> send_videocall_message());

        //친구 삭제 눌렀을 경우
        deletebt.setOnClickListener(v ->{

            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setTitle("친구 차단");
            builder.setMessage(yourname +"님을 정말로 차단하시겠습니까?");
            builder.setPositiveButton("예", (dialog, which) -> {
                //친구 차단
                Call<User> call = apiInterface.getawayfromme(myemail, youremail);
                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()){
                            User user = response.body();
                            if (user.getResponse().equals("delete")){
                                //친구 차단 성공하면 채팅화면으로 이동
                                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                Toast.makeText(ProfileActivity.this, yourname +"님을 차단했습니다.", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(ProfileActivity.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                            }

                        }else {
                            Log.d(TAG, "onResponse: error");
                        }
                    }

                    @Override
                    public void onFailure(Call<User> vcall, Throwable t) {
                        Log.d(TAG, "onFailure: "+t);
                    }
                });

            }).setNegativeButton("아니요", (dialog, which) -> {
               dialog.dismiss();
            });
            builder.show();


        });

        //1:1 채팅 눌렀을 경우
        meandyoubt.setOnClickListener(v -> {
            Call<Room> call = apiInterface.chatroomnum(myemail, youremail);
            call.enqueue(new Callback<Room>() {
                @Override
                public void onResponse(Call<Room> call, Response<Room> response) {
                    if (response.isSuccessful()){

                        Room room = response.body();

                        String rnum = room.getRnum();
                        Log.d(TAG, "onResponse: rnum"+rnum);
                        Intent messageintent= new Intent(ProfileActivity.this, MessageActivity.class);
                        messageintent.putExtra("email",youremail);
                        messageintent.putExtra("name", yourname);
                        messageintent.putExtra("rnum",rnum);
                        messageintent.putExtra("total","1");
                        startActivity(messageintent);


                    }else {
                        Log.d(TAG, "onResponse: error");
                    }
                }

                @Override
                public void onFailure(Call<Room> call, Throwable t) {
                    Log.d(TAG, "onFailure: "+t);
                }
            });


        });
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


    //권한 승인 뒤에
    @AfterPermissionGranted(RC_CALL)
    private void connect() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            //방으로 간다.
            connectToRoom(videoroomstr);
        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
    }

    //
    private void send_videocall_message() {
        //방 번호 랜덤으로 6~8자리에서 생성하고 상대방에게 보낸다.
        double randomvalue= Math.random();
        int roomnum = (int)(randomvalue*10000000)+100000;
        Log.d(TAG, "send_videocall_message: "+roomnum);
        videoroomstr =String.valueOf(roomnum);

        //보내는 시간 얻기
        getymdhm();
        new VideoSender().execute();

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
    //권한 요청 결과를 확인하는 곳
    //권한 요청 이후에 호출된다.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        Log.d(TAG, "onRequestPermissionsResult: ");
    }

    //roomid들 키로하고 사용자가 입력한 방 번호를 가지고
    //영상통화 화면으로 이동한다.
    private void connectToRoom(String roomId) {
        Log.d(TAG, "connectToRoom: "+roomId);
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(EXTRA_ROOMID, roomId);
        intent.putExtra("youremail",youremail);
        startActivityForResult(intent, CONNECTION_REQUEST);
    }


    private class VideoSender extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: hi i'm item_message_sender");

            //사용자 각각 알림에 나를 제외한 사용자를 넣기 위해
            String type ="mtext";
            String total ="1";
            String receiveremail = youremail;
            String message = "videocall";
            String rnum = videoroomstr;

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
            //권한 체크하고
           connect();
        }
    }
}
