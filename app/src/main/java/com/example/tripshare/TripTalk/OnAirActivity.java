package com.example.tripshare.TripTalk;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tripshare.Adapter.StreamChatAdapter;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.Message;
import com.example.tripshare.LiveStream.Livestream;
import com.example.tripshare.LiveStream.SourceConnectionInformation;
import com.example.tripshare.LiveStream.StreamingActivity;
import com.example.tripshare.LiveStream.ui.AutoFocusListener;
import com.example.tripshare.LiveStream.ui.MultiStateButton;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.R;
import com.example.tripshare.Token.config;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import info.bcdev.librarysdkew.interfaces.callback.CBBip44;
import info.bcdev.librarysdkew.interfaces.callback.CBGetCredential;
import info.bcdev.librarysdkew.interfaces.callback.CBLoadSmartContract;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingEther;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingToken;
import info.bcdev.librarysdkew.smartcontract.LoadSmartContract;
import info.bcdev.librarysdkew.wallet.SendingToken;
import info.bcdev.librarysdkew.web3j.Initiate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnAirActivity extends AppCompatActivity implements WOWZStatusCallback, View.OnClickListener,
        CBGetCredential, CBLoadSmartContract, CBBip44, CBSendingEther, CBSendingToken {

    // The top-level GoCoder API interface
    private WowzaGoCoder goCoder;

    // The GoCoder SDK camera view
    private WOWZCameraView goCoderCameraView;

    // The GoCoder SDK audio device
    private WOWZAudioDevice goCoderAudioDevice;

    // The GoCoder SDK broadcaster
    private WOWZBroadcast goCoderBroadcaster;

    // The broadcast configuration settings
    private WOWZBroadcastConfig goCoderBroadcastConfig;

    private MultiStateButton mBtnSwitchCamera = null;
    // Properties needed for Android 6+ permissions handling
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private boolean mPermissionsGranted = true;
    private String[] mRequiredPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO


    };

    public static ApiInterface apiInterface;
    public static PrefConfig prefConfig;

    private String myname, roomname, roomid;
    private static final String TAG = "OnAirActivity";

    private ImageView endimgv, startimgv, tokenimg;
    private EditText roomedit;
    private LinearLayout startlayout;

    private LinearLayout recylinear, befolinear;
    private RecyclerView recyclerchat;
    private EditText chatedit;
    private ImageView chatsendbt;
    InputMethodManager imm;

    private ArrayList<Message> messageArrayList;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String myemail, myurl, message, type;
    private Handler handler;
    private Context mtx;
    private StreamChatAdapter adapter;
    private LottieAnimationView lottieAnimationView, tokenanimation;
    private SourceConnectionInformation firstroom, secondroom;

    private Credentials mycredentials;
    private String mNodeUrl = config.addressethnode(2);
    private String mSmartcontract = config.addresssmartcontract(1);
    private Web3j web3;
    private String othrersaddress;

    //metamask에서 생성된 지갑의 주소, 토큰을 가져오기 위한 private key
    private final static String PRIVATE_KEY = "C0BE164D9ACD9C4290661B95858A3F19156D35F4D4100F960255771ED6EB410D";
    private final static String OUTADDRESS ="0x376Eb2839e8F39aa6c510Fff226101b186B053F4";
    //이더 전송할 때 사용하는 gas, gas limit 전송 속도를 결정
    private final static BigInteger GAS_LIMIT = BigInteger.valueOf(55668L);
    private final static BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L);
    //이더 전송과 충전할 때 사용하는 progressbar
    ProgressDialog tokensendprogress;
    private SendingToken sendingToken;


    // Gestures are used to toggle the focus modes
    protected GestureDetectorCompat mAutoFocusDetector = null;

    private boolean threadagain = false;
    private String walletaddress;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_on_air);

        firstroom = new SourceConnectionInformation(
                "ee8129.entrypoint.cloud.wowza.com",
                "app-0cd3",
                "16229545",
                "client43063",
                "3f47e34b"
        );
        secondroom = new SourceConnectionInformation(
                "7b915e.entrypoint.cloud.wowza.com",
                "app-a23d",
                "5dd9295a",
                "client42837",
                "72f02117"
        );

        lottieAnimationView = findViewById(R.id.heart_lottie_onair);
        //하트 전송이 완료되고 애니메이션이 끝나면 하트 안보이게 하기
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "onAnimationEnd: ");
                lottieAnimationView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mtx = getApplication();

        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                //메세지 도착
                Message message = (Message) msg.obj;
                Log.d(TAG, "handleMessage:메세지 도착 " + message.getMessage());
                showmessage(message);
            }
        };

        //키보드 올라와도 사이즈 조절됨
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //키보드 내리기 위해서 사용함
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

       /* getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        //  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        befolinear = findViewById(R.id.beforeclick_linear_onari);
        recylinear = findViewById(R.id.linearchat_onair);
        chatedit = findViewById(R.id.befor_edit_onair);
        chatsendbt = findViewById(R.id.befor_send_onair);
        recyclerchat = findViewById(R.id.chat_recy_onair);
        tokenimg = findViewById(R.id.tokensend_img_onair);
        tokenanimation = findViewById(R.id.token_lottie_onair);

        //토큰 주고 받는 게 완료됬을 때 토큰 안 보여주기
        tokenanimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "onAnimationStart: ");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "onAnimationEnd: ");
                tokenanimation.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerchat.setLayoutManager(layoutManager);
        messageArrayList = new ArrayList<>();

        chatsendbt.setOnClickListener(v -> {
            if (!chatedit.getText().toString().equals("")) {
                type = "text";
                message = chatedit.getText().toString().trim();
                chatedit.setText("");
                //키보드 내리기
                imm.hideSoftInputFromWindow(chatedit.getWindowToken(), 0);
                //메세지 보내기
                new Sendmessage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        //토큰 개수를 얻기 위한 web3j
        getWeb3j();

        //지갑에 있는 토큰 개수 얻기
        tokenimg.setOnClickListener(v -> GetTokenInfo());


        startlayout = findViewById(R.id.linear_onair);
        endimgv = findViewById(R.id.end_bt_onair);
        startimgv = findViewById(R.id.roomname_bt_onair);
        roomedit = findViewById(R.id.roomname_edit_onair);
        mBtnSwitchCamera = findViewById(R.id.ic_switch_camera_onair);

        mBtnSwitchCamera.setOnClickListener(this);
        startimgv.setOnClickListener(this);
        endimgv.setOnClickListener(this);

        prefConfig = new PrefConfig(this);
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        myname = prefConfig.getName();
        myemail = prefConfig.readEmail();
        myurl = prefConfig.readimgurl();
        Log.d(TAG, "onCreate:myname " + myname);
        Log.d(TAG, "onCreate:myemail " + myemail);
        Log.d(TAG, "onCreate:myurl " + myurl);
        /*roomname = getIntent().getStringExtra("roomname");
        Log.d(TAG, "onCreate:roomname "+roomname);*/

        // Initialize the GoCoder SDK
        goCoder = WowzaGoCoder.init(getApplicationContext(), "GOSK-8246-010C-023D-A279-1DCE");

        if (goCoder == null) {
            // If initialization failed, retrieve the last error and display it
            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
            Toast.makeText(this,
                    "GoCoder SDK error: " + goCoderInitError.getErrorDescription(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Create an audio device instance for capturing and broadcasting audio
        goCoderAudioDevice = new WOWZAudioDevice();

        // Associate the WOWZCameraView defined in the U/I layout with the corresponding class member
        goCoderCameraView = (WOWZCameraView) findViewById(R.id.camera_preview);

        // Associate the onClick() method as the callback for the broadcast button's click event
        //Button broadcastButton = (Button) findViewById(R.id.broadcast_button);
        //broadcastButton.setOnClickListener(this);
        goCoderCameraView.setEnabled(true);
        mBtnSwitchCamera.setOnClickListener(v -> onSwitchCameraOnair());

    }

    /* Get Web3j*/
    private void getWeb3j(){
        Log.d(TAG, "getWeb3j:nodeurl "+mNodeUrl);
        new Initiate(mNodeUrl);
        web3 = Initiate.sWeb3jInstance;
    }

    //지갑의 토큰 개수를 가져옴
    private void GetTokenInfo() {
        LoadSmartContract loadSmartContract = new LoadSmartContract(web3,mycredentials,mSmartcontract,GAS_PRICE,GAS_LIMIT);
        loadSmartContract.registerCallBack(this);
        loadSmartContract.LoadToken();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        try {
            /*type = "exit";
            message = "exit";
            new Sendmessage().execute();*/
            Log.d(TAG, "onDestroy:socketclose ");
            if (socket != null) {
                //나간다는 것 서버에도 알려주기
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }


    Thread connect = new Thread() {
        @Override
        public void run() {

            try {
                socket = new Socket("115.71.238.81", 9999);    //서버로 접속
                Log.d(TAG, "run:connect server ");
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                Log.d(TAG, "run:myemail " + myemail);
                Log.d(TAG, "run:roomid " + roomid);
                //이메일 보낸다.
                dataOutputStream.writeUTF(myemail);
                dataOutputStream.writeUTF(roomid);
                while (true) {
                    String type = dataInputStream.readUTF();
                    String senderemail = dataInputStream.readUTF();
                    String sendername = dataInputStream.readUTF();
                    String senderurl = dataInputStream.readUTF();
                    String message = dataInputStream.readUTF();


                    Log.d(TAG, "run:senderemail " + senderemail);
                    Log.d(TAG, "run:sendername " + sendername);
                    Log.d(TAG, "run:senderurl " + senderurl);
                    Log.d(TAG, "run:type " + type);
                    Log.d(TAG, "run:message " + message);

                    //받은 메세지를 일단 메인 스레드로 보내자.
                    android.os.Message msg = handler.obtainMessage();
                    msg.obj = new Message(senderemail, senderurl, message, sendername, type);
                    handler.sendMessage(msg);

                }
            } catch (IOException e) {
                Log.d(TAG, "run:error " + e.getMessage());
                e.printStackTrace();
            }
        }
    };

    @Override
    public void backGeneration(Map<String, String> result, Credentials credentials) {

    }

    @Override
    public void backLoadCredential(Credentials credentials) {

    }

    //돈 이미지를 클릭하면 지갑에 있는 토큰 개수를 가져오고\
    //가져온 이후에 호출됨
    @Override
    public void backLoadSmartContract(Map<String, String> result) {
        //다이어로그 띄워줘서 현재 토큰 개수 보여줄 것
        String nowtoken = result.get("tokenbalance");
        Log.d(TAG, "backLoadSmartContract:now token "+nowtoken);
        AlertDialog.Builder builder = new AlertDialog.Builder(OnAirActivity.this);
        builder.setTitle("Seung 토큰 개수 확인");
        builder.setMessage("현재 토큰 개수 : "+nowtoken);
        builder.setPositiveButton("확인", (dialog, which) -> {

        });
        builder.show();
    }

    @Override
    public void backSendEthereum(EthSendTransaction result) {

    }

    @Override
    public void backSendToken(TransactionReceipt result) {

    }

    private class Sendmessage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            //message, type, email, name, url
            Log.d(TAG, "doInBackground: " + type);
            Log.d(TAG, "doInBackground: " + myemail);
            Log.d(TAG, "doInBackground: " + myname);
            Log.d(TAG, "doInBackground: " + myurl);
            Log.d(TAG, "doInBackground: " + message);
            try {
                dataOutputStream.writeUTF(type);
                dataOutputStream.writeUTF(myemail);
                dataOutputStream.writeUTF(myname);
                dataOutputStream.writeUTF(myurl);
                dataOutputStream.writeUTF(message);
                dataOutputStream.writeUTF(roomid);
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Message messageobj = new Message(myemail, myurl, message, myname, type);
            showmessage(messageobj);
            if (type.equals("exit")) {
                //진행자가 나감 버튼을 누르면 방송 종료
                goout();
            }
        }
    }

    private void showmessage(Message message) {

        //텍스트인 경우
        Log.d(TAG, "showmessage: " + message.getType());
        Log.d(TAG, "showmessage: " + message.getMessage());
        if (message.getType().equals("text") || message.getType().equals("token")) {
            //받은 것이 메세지거나 토큰일 경우
            //채팅방에 보여준다.
            Log.d(TAG, "showmessage:text or token " + message.getMessage());

            if (messageArrayList.size() == 0) {
                Log.d(TAG, "showmessage:size 0 ");
                messageArrayList.add(message);
                adapter = new StreamChatAdapter(mtx, messageArrayList);
                recyclerchat.setAdapter(adapter);
            } else {
                Log.d(TAG, "showmessage:size not 0 ");
                messageArrayList.add(message);
                adapter = new StreamChatAdapter(mtx, messageArrayList);
                recyclerchat.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                recyclerchat.getLayoutManager().scrollToPosition(messageArrayList.size() - 1);
            }

            if (message.getType().equals("token")){
                //토큰인 경우엔 애니메이션 보이게 하기
                Log.d(TAG, "showmessage:token");
                tokenanimation.setVisibility(View.VISIBLE);
                tokenanimation.setAnimation("star.json");
                tokenanimation.playAnimation();
            }

        } else if (message.getType().equals("heart")) {
            Log.d(TAG, "showmessage:heart ");
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.setAnimation("heartt.json");
            lottieAnimationView.playAnimation();
        }
    }

    //
    // Enable Android's immersive, sticky full-screen mode
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "onWindowFocusChanged: ");
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            Log.d(TAG, "onWindowFocusChanged:rootview not null ");
        } else {
            Log.d(TAG, "onWindowFocusChanged:rootview null ");
        }
    }


    //
    // Called when an activity is brought to the foreground
    //
    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: ");
        // If running on Android 6 (Marshmallow) and later, check to see if the necessary permissions
        // have been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionsGranted = hasPermissions(this, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        } else
            mPermissionsGranted = true;

        if (mAutoFocusDetector == null)
            mAutoFocusDetector = new GestureDetectorCompat(this, new AutoFocusListener(this, goCoderCameraView));

        WOWZCamera activeCamera = goCoderCameraView.getCamera();
        if (activeCamera != null && activeCamera.hasCapability(WOWZCamera.FOCUS_MODE_CONTINUOUS))
            activeCamera.setFocusMode(WOWZCamera.FOCUS_MODE_CONTINUOUS);


        // Start the camera preview display
        if (mPermissionsGranted && goCoderCameraView != null) {

            Log.d(TAG, "onResume: "+goCoderCameraView.getBroadcasterStatus());

            if (goCoderCameraView.isPreviewPaused()) {
                Log.d(TAG, "onResume:resume is previewpaused");
                //goCoderCameraView.startBroadcasting();
                // startbroad(roomname);
                //방송 시작
                //Toast.makeText(mtx, goCoderCameraView.getBroadcasterStatus().toString(), Toast.LENGTH_SHORT).show();

                //goCoderCameraView.onResume();
                goCoderCameraView.onResume();
                //goCoderCameraView.startPreview();
                //goCoderCameraView.startBroadcasting();
            }else{
                Log.d(TAG, "onResume:start preview ");
                goCoderCameraView.startPreview();
          }
        }

        // Create a broadcaster instance
        goCoderBroadcaster = new WOWZBroadcast();


        // Create a configuration instance for the broadcaster
        goCoderBroadcastConfig = new WOWZBroadcastConfig(WOWZMediaConfig.FRAME_SIZE_1920x1080);

        // Set the connection properties for the target Wowza Streaming Engine server or Wowza Streaming Cloud live stream
        goCoderBroadcastConfig.setHostAddress("ee8129.entrypoint.cloud.wowza.com");
        goCoderBroadcastConfig.setPortNumber(1935);
        goCoderBroadcastConfig.setApplicationName("app-0cd3");
        goCoderBroadcastConfig.setStreamName("16229545");
        goCoderBroadcastConfig.setUsername("client43063");
        goCoderBroadcastConfig.setPassword("3f47e34b");

        // Designate the camera preview as the video source
        goCoderBroadcastConfig.setVideoBroadcaster(goCoderCameraView);

        // Designate the audio device as the audio broadcaster
        goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);

        if (threadagain){
            //외부 화면을 갔다가 다시 들어온 경우
            //goCoderCameraView.onResume();
            Log.d(TAG, "onResume:after onpause ");
            Log.d(TAG, "onResume:시작 전 방송 상태 "+goCoderCameraView.getBroadcasterStatus());
            //goCoderCameraView.onResume();
            //startbroad(roomname);
            Log.d(TAG, "onResume:방송 사태 "+goCoderBroadcaster.getStatus());
        }


      /*  Log.d(TAG, "onResume:room "+roomname);
        if (threadagain){
            startbroad(roomname);
        }*/

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mAutoFocusDetector != null)
            mAutoFocusDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }


// Callback invoked in response to a call to ActivityCompat.requestPermissions() to interpret
// the results of the permissions request
//
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mPermissionsGranted = true;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // Check the result of each permission granted
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = false;
                    }
                }
            }
        }
    }

    //
// Utility method to check the status of a permissions request for an array of permission identifiers
//
    private static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions)
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }

    //
// The callback invoked upon changes to the state of the broadcast
//
    @Override
    public void onWZStatus(final WOWZStatus goCoderStatus) {
        // A successful status transition has been reported by the GoCoder SDK
        final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");

        switch (goCoderStatus.getState()) {
            case WOWZState.STARTING:
                System.out.println("onWZStatus : Starting");
                Log.d(TAG, "onWZStatus:start ");
                break;

            case WOWZState.READY:
                System.out.println("onWZStatus : ready");
                Log.d(TAG, "onWZStatus:ready ");
                //statusMessage.append("방송이 준비 중 입니다.");
                break;

            case WOWZState.RUNNING:
                System.out.println("onWZStatus : running");
                Log.d(TAG, "onWZStatus:running ");
                //statusMessage.append("방송을 시작했습니다. ");
                String message = "방송을 시작했습니다.";

                // Display the status message using the U/I thread
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(OnAirActivity.this, message, Toast.LENGTH_LONG).show());
                break;

            case WOWZState.STOPPING:
                System.out.println("onWZStatus : stopping");
                Log.d(TAG, "onWZStatus:stopping ");
                //statusMessage.append("방송을 중지했습니다.");
                String amessage = "방송을 중지했습니다.";
                //Toast.makeText(this, "방송을 중지했습니다.", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(OnAirActivity.this, amessage, Toast.LENGTH_LONG).show());
                break;

            case WOWZState.IDLE:
                System.out.println("onWZStatus : idle");
                Log.d(TAG, "onWZStatus:idle ");
//                statusMessage.append("The broadcast is stopped");
                break;
            case WOWZState.COMPLETE:
                System.out.println("onWZStatus : complete");
                Log.d(TAG, "onWZStatus:complete ");
                break;
            case WOWZState.ERROR:
                System.out.println("onWZStatus : error");
                Log.d(TAG, "onWZStatus:error ");
                break;
            case WOWZState.PAUSED:
                System.out.println("onWZStatus : pause");
                Log.d(TAG, "onWZStatus:pause ");
                break;
            case WOWZState.DECODER_ENDED:
                System.out.println("onWZStatus : decoder_ended");
                Log.d(TAG, "onWZStatus:decoder_ended ");
                break;
            case WOWZState.STOPPED:
                System.out.println("onWZStatus : stoppend");
                Log.d(TAG, "onWZStatus:stopped ");
                break;
            case WOWZState.SHUTDOWN:
                System.out.println("onWZStatus : shutdown");
                Log.d(TAG, "onWZStatus:shutdown ");
                break;
            case WOWZState.MAX_STATE:
                System.out.println("onWZStatus : max state");
                Log.d(TAG, "onWZStatus:max state ");
                break;
            case WOWZState.DECODER_STARTED:
                System.out.println("onWZStatus : decoder start");
                Log.d(TAG, "onWZStatus:decoder start ");
                break;
            case WOWZState.PREBUFFERING_STARTED:
                System.out.println("onWZStatus : pre started");
                Log.d(TAG, "onWZStatus: prebuffering started ");
                break;
            case WOWZState.UNKNOWN:
                Log.d(TAG, "onWZStatus:unknown ");
                System.out.println("onWZStatus : unknown");
                break;
            default:
        }
    }

    /**
     * Click handler for the switch camera button
     */
    public void onSwitchCameraOnair() {
        if (goCoderCameraView == null) return;

        WOWZCamera newCamera = goCoderCameraView.switchCamera();
        if (newCamera != null) {
            if (newCamera.hasCapability(WOWZCamera.FOCUS_MODE_CONTINUOUS))
                newCamera.setFocusMode(WOWZCamera.FOCUS_MODE_CONTINUOUS);
        }
    }

    //방 정보를 서버에 저장한다.
    //방 정보는 방 이름, 스트리머 이름, 스트리머 지갑 주소 등이다.
    private void savelivestream() {

        //토큰을 받을 지갑 주소 얻어오기
        getwalletaddress();

        //스트리머 이름, 방제목, 지갑 주소를 서버에 저장한다.
        Call<Livestream> call = apiInterface.saveroom(myname, roomname, walletaddress);
        call.enqueue(new Callback<Livestream>() {
            @Override
            public void onResponse(Call<Livestream> call, Response<Livestream> response) {
                if (response.isSuccessful()) {
                    Livestream livestream = response.body();
                    Log.d(TAG, "onResponse: save  " + livestream.getResponse());
                    //방송 시작
                    startbroad(livestream.getResponse());
                }
            }
            @Override
            public void onFailure(Call<Livestream> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }
    //토큰을 받을 지갑 주소 얻어오기
    private void getwalletaddress() {
        //쉐어드에 지갑이 저장되있으면 지갑의 파일명을 가져온다.
        //지갑의 파일명은 지갑의 주소를 가져올 때 사용된다.
        String walletfilename = prefConfig.readwallet();
        if (!walletfilename.equals("")) {
            Log.d(TAG, "getwallet:get ");

            try {
                //지갑 주소를 가져올 때 사용하는 패스워드
                String password = "seung";
                mycredentials = WalletUtils.loadCredentials(password, walletfilename);
                // 지갑 주소
                walletaddress = mycredentials.getAddress();
                Log.d(TAG, "getwalletaddress:address " + walletaddress);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (CipherException e) {
                e.printStackTrace();
            }

        }
    }
    //방송을 시작해 와우자 스트리밍 클라우드를 이용해 다른 사용자에게 알린다.
    private void startbroad(String roomname) {
        if (roomname.equals("first")) {
            roomid = "first";
            // Set the connection properties for the target Wowza Streaming Engine server or Wowza Streaming Cloud live stream
            goCoderBroadcastConfig.setHostAddress(firstroom.getPrimaryServer());
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName(firstroom.getApplication());
            goCoderBroadcastConfig.setStreamName(firstroom.getStreamName());
            goCoderBroadcastConfig.setUsername(firstroom.getUsername());
            goCoderBroadcastConfig.setPassword(firstroom.getPassword());
        } else {
            roomid = "second";
            goCoderBroadcastConfig.setHostAddress(secondroom.getPrimaryServer());
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName(secondroom.getApplication());
            goCoderBroadcastConfig.setStreamName(secondroom.getStreamName());
            goCoderBroadcastConfig.setUsername(secondroom.getUsername());
            goCoderBroadcastConfig.setPassword(secondroom.getPassword());
        }
        //상황을 받는 거지
        goCoderBroadcaster.setStatusCallback(new StatusCallback());
        //방송 시작
        goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);


        if (threadagain){
            // 두 번째 연결하는 경우
            Log.d(TAG, "startbroad:소켓 미연결 ");
        }else {
            Log.d(TAG, "startbroad:소켓 연결 ");
            //처음 연결 하는 경우
            //채팅을 위한 내 서버와 소켓 연결
            connect.setDaemon(true);
            connect.start();
        }
    }

    //
// The callback invoked when an error occurs during a broadcast
//
    @Override
    public void onWZError(final WOWZStatus goCoderStatus) {
        // If an error is reported by the GoCoder SDK, display a message
        // containing the error details using the U/I thread
        Log.d(TAG, "onWZError: " + goCoderStatus.getLastError());
        System.out.println("onWZError : " + goCoderStatus.getLastError());
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(OnAirActivity.this,
                "Streaming error: " + goCoderStatus.getLastError().getErrorDescription(),
                Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onPause() {
        super.onPause();
        threadagain = true;

        if (type == null){
            //홈 키를 눌러서 다른 화면으로 간 경우
            //방송을 종료하고 카메라 미리보기를 종료한다.
            goCoderBroadcaster.endBroadcast(this);
            goCoderCameraView.stopPreview();
            goCoderCameraView.stopBroadcasting();

            Log.d(TAG, "onPause:type null ");

            //방송 종료와 방 이름 삭제
            Call<Livestream> call = apiInterface.deleteroom(roomname);
            call.enqueue(new Callback<Livestream>() {
                @Override
                public void onResponse(Call<Livestream> call, Response<Livestream> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "onpause :delete " + response.body().getResponse());

                    }
                }
                @Override
                public void onFailure(Call<Livestream> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t);
                }
            });
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        System.out.println("onConfigurationChanged : Starting");
        Log.d(TAG, "onConfigurationChanged: ");
    }


    private void deleteroom(){
        //방송 종료와 방 이름 삭제
        Call<Livestream> call = apiInterface.deleteroom(roomname);
        call.enqueue(new Callback<Livestream>() {
            @Override
            public void onResponse(Call<Livestream> call, Response<Livestream> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse:delete " + response.body().getResponse());
                    // Display the status message using the U/I thread
                    // Stop the broadcast that is currently running
                    //채팅 서버에게 나간다고 알려주기
                    type = "exit";
                    message = "exit";
                    new Sendmessage().execute();
                    //goout();
                }
            }
            @Override
            public void onFailure(Call<Livestream> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (type != null){

            AlertDialog.Builder builder = new AlertDialog.Builder(OnAirActivity.this);
            builder.setTitle("방송 종료");
            builder.setMessage("방송을 종료하시겠습니까?");
            builder.setPositiveButton("예", (dialog, which) -> {
                deleteroom();
            });
            builder.setNegativeButton("아니요", (dialog, which) -> {

            });
            builder.show();

        }else {

            goCoderBroadcaster.endBroadcast(this);
            goCoderCameraView.stopBroadcasting();
            goCoderCameraView.clearView();
            goCoderCameraView.stopPreview();
            Intent intent = new Intent(OnAirActivity.this, StreamingActivity.class);
            intent.putExtra("method", "delete");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            Log.d(TAG, "goback:false click finish ");
            finish();
        }
    }

    //
    // The callback invoked when the broadcast button is tapped
    //
    @Override
    public void onClick(View view) {
        // return if the user hasn't granted the app the necessary permissions
        if (!mPermissionsGranted) return;

        // Ensure the minimum set of configuration settings have been specified necessary to
        // initiate a broadcast streaming session
        WOWZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();

        if (configValidationError != null) {
            Toast.makeText(this, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
        } else if (goCoderBroadcaster.getStatus().isRunning()) {
            //방송 진행중에 방송 종료를 선택한 경우
            //방 서버에서 삭제
            AlertDialog.Builder builder = new AlertDialog.Builder(OnAirActivity.this);
            builder.setTitle("방송 종료");
            builder.setMessage("방송을 종료하시겠습니까?");
            builder.setPositiveButton("예", (dialog, which) -> {
                deleteroom();
            });
            builder.setNegativeButton("아니요", (dialog, which) -> {

            });
            builder.show();


        } else if (threadagain) {
            //방송에서 홈화면 나간 이후 다시 들어왔을 클릭하면 방을 삭제하고 나간다.
            deleteroom();
        }else {
                if (roomedit.getText().toString().equals("")) {
                    Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    roomname = roomedit.getText().toString();
                    //서버에 방을 저장한다.
                    Log.d(TAG, "onClick: " + roomname);
                    savelivestream();
                }

        }
        switch (view.getId()) {
            case R.id.roomname_bt_onair:
                goCoderCameraView.setEnabled(true);
                //방 이름 저장과 시작
                //방 이름 입력과 버튼은 사라지게 하고 종료 버튼은 보이게
                startlayout.setVisibility(View.GONE);
                endimgv.setVisibility(View.VISIBLE);
                recylinear.setVisibility(View.VISIBLE);
                //키보드 내리기
                imm.hideSoftInputFromWindow(roomedit.getWindowToken(), 0);
                break;
            case R.id.end_bt_onair:
                break;
        }
    }

    //진행자가 나감 버튼을 누른 경우
    //1. 채팅 소켓을 닫는다.
    //2. 방송을 종료한다.
    //3. 화면에서 나간다.
    private void goout() {
        try {
            if (socket != null) {
                Log.d(TAG, "onPause:socketclose ");
                socket.close();
            }
            //goCoderBroadcaster.endBroadcast();
            goCoderBroadcaster.endBroadcast(this);
            goCoderCameraView.stopBroadcasting();
            goCoderCameraView.clearView();
            goCoderCameraView.stopPreview();
            Log.d(TAG, "goout:finsh ");
            Intent intent = new Intent(OnAirActivity.this, StreamingActivity.class);
            intent.putExtra("method", "delete");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            Log.d(TAG, "goback:false click finish ");
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
