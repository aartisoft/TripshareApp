/**
 * This is sample code provided by Wowza Media Systems, LLC.  All sample code is intended to be a reference for the
 * purpose of educating developers, and is not intended to be used dataInputStream any production environment.
 * <p>
 * IN NO EVENT SHALL WOWZA MEDIA SYSTEMS, LLC BE LIABLE TO YOU OR ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
 * OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION,
 * EVEN IF WOWZA MEDIA SYSTEMS, LLC HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * WOWZA MEDIA SYSTEMS, LLC SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. ALL CODE PROVIDED HEREUNDER IS PROVIDED "AS IS".
 * WOWZA MEDIA SYSTEMS, LLC HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * <p>
 * © 2015 – 2019 Wowza Media Systems, LLC. All rights reserved.
 */

package com.example.tripshare.LiveStream;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tripshare.Adapter.StreamChatAdapter;
import com.example.tripshare.Data.Message;
import com.example.tripshare.LiveStream.config.GoCoderSDKPrefs;
import com.example.tripshare.LiveStream.ui.DataTableFragment;
import com.example.tripshare.LiveStream.ui.MultiStateButton;
import com.example.tripshare.LiveStream.ui.StatusView;
import com.example.tripshare.LiveStream.ui.VolumeChangeObserver;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.R;
import com.example.tripshare.Token.config;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.data.WOWZDataMap;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.geometry.WOWZSize;
import com.wowza.gocoder.sdk.api.logging.WOWZLog;
import com.wowza.gocoder.sdk.api.player.GlobalPlayerStateManager;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;

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


public class PlayerActivity extends GoCoderSDKActivityBase implements WOWZCameraView.PreviewStatusListener,
    CBGetCredential, CBLoadSmartContract, CBBip44, CBSendingEther, CBSendingToken {
    final private static String TAG = PlayerActivity.class.getSimpleName();
    final private static String TAGgg = "playerAA";
    // Stream player view
    private WOWZPlayerView mStreamPlayerView = null;
    private WOWZPlayerConfig mStreamPlayerConfig = null;

    // UI controls
    private MultiStateButton mBtnPlayStream = null;
    private MultiStateButton mBtnSettings = null;
    //private MultiStateButton mBtnMic = null;
    //private MultiStateButton mBtnScale = null;
    //private SeekBar mSeekVolume = null;
    private ProgressDialog mBufferingDialog = null;
    private ProgressDialog mGoingDownDialog = null;
    private StatusView mStatusView = null;
    private TextView mHelp = null;
    // private TimerView mTimerView = null;
    //private ImageButton mStreamMetadata = null;
    private VolumeChangeObserver mVolumeSettingChangeObserver = null;
    int justone, bufferone;

    private RecyclerView recyclerView;
    private EditText messageedit;
    private ImageView sendimg, heartimg, tokenimg;

    private ArrayList<Message> messageArrayList;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public static PrefConfig prefConfig;
    private String myemail, myname, myurl, message, type, roomid;
    private InputMethodManager imm;
    private Handler handler;
    private StreamChatAdapter adapter;
    private Context mtx;
    private boolean clickgoout = false;
    private LottieAnimationView lottieAnimationView, tokenanimation;

    SourceConnectionInformation sourceConnectionInformation;
    private String mywalletfilename, mywalletaddress, streameraddress;


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
    String tokenamount;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_player);

      /*  networkChangeReceiver = new NetworkChangeReceiver(this);
        networkChangeReceiver.setOnChangeNetworkStatusListener(NetworkChangedListener);

        registerReceiver(networkChangeReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
*/
        lottieAnimationView = findViewById(R.id.heart_lottie_player);
        //하트 전송이 끝나면 하트 애니메이션 안보이게 하기
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

        sourceConnectionInformation = (SourceConnectionInformation) getIntent().getSerializableExtra("room");
        Log.d(TAG, "onCreate: " + sourceConnectionInformation.getApplication());

        if (sourceConnectionInformation.getStreamName().equals("16229545")) {
            roomid = "first";
            //서버에서 받은 스트리머의 지갑 주소가 있다면 받는다.
            if (sourceConnectionInformation.getWalletaddress() != null) {
                streameraddress = sourceConnectionInformation.getWalletaddress();
                Log.d(TAG, "onCreate:streameraddress "+streameraddress);
            }
        } else {
            roomid = "second";
        }
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


        Log.d(TAG, "onCreate:playerActivity ");
        mRequiredPermissions = new String[]{};

        prefConfig = new PrefConfig(this);
        myemail = prefConfig.readEmail();
        myname = prefConfig.getName();
        myurl = prefConfig.readimgurl();
        //사용자가 지갑을 만들어서 저장했다면 지갑 파일의 이름을 얻어온다.
        if (prefConfig.readwallet() != null){
            mywalletfilename = prefConfig.readwallet();
            Log.d(TAG, "onCreate:walletname "+mywalletfilename);

            //내 지갑의 주소를 얻어온다.
            getmyaddress();
        }
        Log.d(TAG, "onCreate:email " + myemail);
        Log.d(TAG, "onCreate:name " + myname);
        Log.d(TAG, "onCreate:url " + myurl);


        //키보드 내리기 위해서 사용함
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        messageArrayList = new ArrayList<>();
        join.setDaemon(true);
        join.start();

        recyclerView = findViewById(R.id.chat_recy_player);
        messageedit = findViewById(R.id.chat_edit_player);
        sendimg = findViewById(R.id.chat_send_player);
        heartimg = findViewById(R.id.chat_heart_player);
        tokenimg = findViewById(R.id.tokensend_img_onair);
        tokenanimation = findViewById(R.id.token_lottie_player);

        //토큰 전송이 완료되면 애니메이션 안보이게
        tokenanimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

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

        sendimg.setOnClickListener(v -> {
            if (!messageedit.getText().toString().equals("")) {
                //무언가 입력이 되있으면
                message = messageedit.getText().toString().trim();
                type = "text";
                //다른 사람에게 전송
                messageedit.setText("");
                //키보드 내리기
                imm.hideSoftInputFromWindow(messageedit.getWindowToken(), 0);
                new Sendmessage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        heartimg.setOnClickListener(v -> {
            message = "heart";
            type = "heart";
            new Sendmessage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        });

        //토큰 개수 확인을 위한 web3j 객체 얻기
        getWeb3j();

        //토큰 개수 확인과 스트리머에게 전송하기
        tokenimg.setOnClickListener(v -> GetTokenInfo());


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mStreamPlayerView = (WOWZPlayerView) findViewById(R.id.vwStreamPlayer);

        mBtnPlayStream = (MultiStateButton) findViewById(R.id.ic_end_stream);
        mBtnSettings = (MultiStateButton) findViewById(R.id.ic_settings);
        /* mBtnMic = (MultiStateButton) findViewById(R.id.ic_mic);
         */// mBtnScale = (MultiStateButton) findViewById(R.id.ic_scale);

        //mTimerView = (TimerView) findViewById(R.id.txtTimer);
        mStatusView = (StatusView) findViewById(R.id.statusView);
        // mStreamMetadata = (ImageButton) findViewById(R.id.imgBtnStreamInfo);
        //mHelp = (TextView) findViewById(R.id.streamPlayerHelp);

        //mSeekVolume = (SeekBar) findViewById(R.id.sb_volume);

        // mTimerView.setVisibility(View.GONE);


        mBtnPlayStream.setOnClickListener(v -> {
            clickgoout = true;
            onTogglePlayStream();
            //stopplaystream();
            // onTogglePlayStream();
        });

        if (sGoCoderSDK != null) {

            /*
            Packet change listener setup
             */
            final PlayerActivity activity = this;
            WOWZPlayerView.PacketThresholdChangeListener packetChangeListener = new WOWZPlayerView.PacketThresholdChangeListener() {
                @Override
                public void packetsBelowMinimumThreshold(int packetCount) {
                    Log.d(TAG, "anything Packets have fallen below threshold " + packetCount);
                    WOWZLog.debug("Packets have fallen below threshold " + packetCount + "... ");

//                    activity.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Toast.makeText(activity, "Packets have fallen below threshold ... ", Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }

                @Override
                public void packetsAboveMinimumThreshold(int packetCount) {
                    Log.d(TAG, "anything Packets have risen above threshold " + packetCount);
                    WOWZLog.debug("Packets have risen above threshold " + packetCount + " ... ");

//                    activity.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Toast.makeText(activity, "Packets have risen above threshold ... ", Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }
            };
            mStreamPlayerView.setShowAllNotificationsWhenBelowThreshold(false);
            mStreamPlayerView.setMinimumPacketThreshold(20);
            mStreamPlayerView.registerPacketThresholdListener(packetChangeListener);
            ///// End packet change notification listener

            // The streaming player configuration properties
            mStreamPlayerConfig = new WOWZPlayerConfig();

            mBufferingDialog = new ProgressDialog(this);
            mBufferingDialog.setTitle(R.string.status_buffering);
            mBufferingDialog.setMessage(getResources().getString(R.string.msg_please_wait));
            // mBufferingDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.button_cancel), (dialogInterface, i) -> cancelBuffering());

            mGoingDownDialog = new ProgressDialog(this);
            mGoingDownDialog.setTitle(R.string.status_buffering);
            mGoingDownDialog.setMessage("잠시만 기다려주세요");

            mStreamPlayerView.registerDataEventListener("onClientConnected", (eventName, eventParams) -> {
                Log.d(TAG, "anything onClientConnected data event received:\n" + eventParams.toString(true));
                WOWZLog.info(TAG, "onClientConnected data event received:\n" + eventParams.toString(true));

                new Handler(Looper.getMainLooper()).post(() -> {

                });

                // this demonstrates how to return a function result back to the original Wowza Streaming Engine
                // function call request
                WOWZDataMap functionResult = new WOWZDataMap();
                functionResult.put("greeting", "Hello New Client!");

                return functionResult;
            });

            // testing player data event handler.
            mStreamPlayerView.registerDataEventListener("onWowzaData", (eventName, eventParams) -> {
                String meta = "";
                if (eventParams != null)
                    meta = eventParams.toString();

                Log.d(TAG, "anything onCreate: " + "onWZDataEvent -> eventName " + eventName + " = " + meta);
                WOWZLog.debug("onWZDataEvent -> eventName " + eventName + " = " + meta);

                return null;
            });

            // testing player data event handler.
            mStreamPlayerView.registerDataEventListener("onStatus", (eventName, eventParams) -> {
                if (eventParams != null) {

                    WOWZLog.debug("onWZDataEvent -> eventName " + eventName + " = " + eventParams.toString());
                    Log.d(TAG, "anything onCreate: " + "onWZDataEvent -> eventName " + eventName + " = " + eventParams.toString());
                }
                return null;

            });


            // testing player data event handler.
            mStreamPlayerView.registerDataEventListener("onTextData", (eventName, eventParams) -> {
                if (eventParams != null) {
                    Log.d(TAG, "anything " + "onWZDataEvent -> " + eventName + " = " + eventParams.get("text"));
                    WOWZLog.debug("onWZDataEvent -> " + eventName + " = " + eventParams.get("text"));
                }

                return null;
            });
        } else {
            mHelp.setVisibility(View.GONE);
            mStatusView.setErrorMessage(WowzaGoCoder.getLastError().getErrorDescription());
        }

    }

    //토큰 전송과 개수 확인
    private void tokensend() {


    }

    //지갑의 주소를 얻어온다.
    private void getmyaddress() {
        try {
            String password = "seung";
            mycredentials = WalletUtils.loadCredentials(password, mywalletfilename);
            //지갑 주소
            mywalletaddress = mycredentials.getAddress();
            Log.d(TAG, "getmyaddress:address " + mywalletaddress);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
    }




    private void stopplaystream() {
        Log.d(TAGgg, "stopplaystream: ");
        showTearingdownDialog();
        mStreamPlayerView.stop();
        mStreamPlayerView.getCurrentStatus().waitForState(WOWZState.IDLE);
    }

    Thread join = new Thread() {
        @Override
        public void run() {

            try {
                socket = new Socket("115.71.238.81", 9999);    //서버로 접속
                Log.d(TAG, "run:connect server ");
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                Log.d(TAG, "run:myemail " + myemail);
                Log.d(TAG, "run:roomid " + roomid);
                //이메일, 방번호 보낸다.
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
    public void onWZCameraPreviewStarted(WOWZCamera wowzCamera, WOWZSize wowzSize, int i) {
        Log.d(TAG, "onWZCameraPreviewStarted: " + i);
    }

    @Override
    public void onWZCameraPreviewStopped(int i) {
        Toast.makeText(mtx, "camera preview stopped" + i, Toast.LENGTH_LONG).show();
        Log.d(TAG, "onWZCameraPreviewStopped: " + i);
    }

    @Override
    public void onWZCameraPreviewError(WOWZCamera wowzCamera, WOWZError wowzError) {
        Log.d(TAG, "onWZCameraPreviewError: " + wowzError.toString());
        Toast.makeText(mtx, "camera preview error" + wowzError.getErrorDescription(), Toast.LENGTH_LONG).show();
    }

    /* Get Web3j*/
    private void getWeb3j(){
        Log.d(TAG, "getWeb3j:nodeurl "+mNodeUrl);
        new Initiate(mNodeUrl);
        web3 = Initiate.sWeb3jInstance;
    }
    @Override
    public void backGeneration(Map<String, String> result, Credentials credentials) {

    }


    @Override
    public void backLoadCredential(Credentials credentials) {
        Log.d(TAG, "backLoadCredential:");
        //GetTokenInfo();
    }
    //지갑의 토큰 개수 가져오기
    private void GetTokenInfo() {
        LoadSmartContract loadSmartContract = new LoadSmartContract(web3,mycredentials,mSmartcontract,GAS_PRICE,GAS_LIMIT);
        loadSmartContract.registerCallBack(this);
        loadSmartContract.LoadToken();
    }

    //지갑의 토큰의 개수를 얻을 때 호출 됨(처음 and 버튼 클릭했을 때)
    @Override
    public void backLoadSmartContract(Map<String, String> result) {
        Log.d(TAG, "backLoadSmartContract: ");
        String tokenbalance = result.get("tokenbalance");
        Log.d(TAG, "backLoadSmartContract:tokenbalance "+tokenbalance);

        EditText tokenedit = new EditText(this);
        tokenedit.setInputType(0x00000002);
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
        builder.setTitle("Seung 토큰 전송");
        builder.setMessage("현재 토큰 개수 : "+tokenbalance);
        builder.setView(tokenedit);
        builder.setNegativeButton("취소", (dialog, which) -> {

        });
        builder.setPositiveButton("전송", (dialog, which) -> {
            //토큰 입력 개수
            tokenamount = tokenedit.getText().toString();
            Log.d(TAG, "receivetoken:토큰 수"+tokenamount);



            int mytokenbalance = Integer.valueOf(tokenbalance);
            int sendamount = Integer.valueOf(tokenamount);
            if (sendamount > mytokenbalance){

                Toast.makeText(mtx, "토큰이 부족합니다.", Toast.LENGTH_SHORT).show();
            }else {
                //토큰 보내기
                sendingToken = new SendingToken(web3,
                        mycredentials,
                        GAS_PRICE.toString(),
                        GAS_LIMIT.toString());
                sendingToken.registerCallBackToken(this);
                //토큰 smartcontract 주소와, 토큰을 받을 스트리머 주소, 토큰 개수를 보낸다.
                sendingToken.Send(mSmartcontract,streameraddress,tokenamount);

                tokensendprogress = new ProgressDialog(PlayerActivity.this);
                tokensendprogress.setMessage(tokenamount+" 토큰 전송 중입니다.");
                tokensendprogress.show();
            }


        });
        builder.show();

    }
    //다른 사용자들에게 토큰 전송했다고 알려주기(애니메이션, 채팅창)
    private void sendmessagetoken() {
        type = "token";
        message = tokenamount;
        new Sendmessage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void backSendEthereum(EthSendTransaction result) {
        Log.d(TAG, "backSendEthereum: ");
    }

    //토큰을 전송 완료한 이후에 호출됨
    @Override
    public void backSendToken(TransactionReceipt result) {
        Log.d(TAG, "backSendToken: ");
        //토큰 전송중이라는 다이어로그 없에
        tokensendprogress.dismiss();
        //다른 사용자들에게 토큰 전송했다고 알려주기(애니메이션, 채팅창)
        sendmessagetoken();
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

        }
    }

    private void showmessage(Message message) {
        //텍스트인 경우
        if (message.getType().equals("text") || message.getType().equals("token")) {
            Log.d(TAG, "showmessage:text " + message.getMessage());

            if (messageArrayList.size() == 0) {
                messageArrayList.add(message);
                adapter = new StreamChatAdapter(mtx, messageArrayList);
                recyclerView.setAdapter(adapter);
            } else {
                messageArrayList.add(message);
                adapter = new StreamChatAdapter(mtx, messageArrayList);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                recyclerView.getLayoutManager().scrollToPosition(messageArrayList.size() - 1);
            }

            if (message.getType().equals("token")){
                Log.d(TAG, "showmessage:token ");
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

    @Override
    protected void onDestroy() {
        if (mVolumeSettingChangeObserver != null)
            getApplicationContext().getContentResolver().unregisterContentObserver(mVolumeSettingChangeObserver);

        super.onDestroy();

        /*//리시버 해제
        unregisterReceiver(networkChangeReceiver);
   */
    }

    /**
     * Android Activity class methods
     */
    @Override
    protected void onResume() {
        super.onResume();

        justone = 0;
        bufferone = 0;
        hideBuffering();
        if (!GlobalPlayerStateManager.isReady()) {
            Log.d(TAGgg, "onResume:GlobalPlayerStateManager.isReady() false ");
            showTearingdownDialog();
            mStreamPlayerView.stop();
        } else {
            Log.d(TAGgg, "onResume:GlobalPlayerStateManager.isReady() true ");
            syncUIControlState();
        }

        onTogglePlayStream();
    }

    @Override
    protected void onPause() {
        try {
            justone = -1;
            Log.d(TAGgg, "onpause:socket down ");
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mStreamPlayerView != null) {

            AsyncTask.execute(() -> {
                WOWZLog.debug("DECODER STATUS Stopping ... ");
                mStreamPlayerView.stop();
                // Wait for the streaming player to disconnect and shutdown...
                mStreamPlayerView.getCurrentStatus().waitForState(WOWZState.IDLE);
                WOWZLog.debug("DECODER STATUS Stopped!  ");
            });
        }

        super.onPause();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null)
            return false;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /*
    Click handler for network pausing
     */
    /*public void onPauseNetwork(View v) {
        Button btn = (Button) findViewById(R.id.pause_network);
        if (btn.getText().toString().trim().equalsIgnoreCase("pause network")) {
            WOWZLog.info("Pausing network...");
            btn.setText(R.string.wz_unpause_network);
            mStreamPlayerView.pauseNetworkStack();
        } else {
            WOWZLog.info("Unpausing network... btn.getText(): " + btn.getText());
            btn.setText(R.string.wz_pause_network);
            mStreamPlayerView.unpauseNetworkStack();
        }
    }*/

    public void playStream() {
        if (!this.isNetworkAvailable()) {
            // Toast.makeText(this, "No internet connection, please try again later.", Toast.LENGTH_SHORT).show();
            //     displayErrorDialog("No internet connection, please try again later.");
            return;
        }

        //mHelp.setVisibility(View.GONE);

        WOWZStreamingError configValidationError = mStreamPlayerConfig.validateForPlayback();
        if (configValidationError != null) {
            mStatusView.setErrorMessage(configValidationError.getErrorDescription());
            Log.d(TAGgg, "playStream:error exist " + configValidationError.getErrorDescription());
        } else {
            // Set the detail level for network logging output
            mStreamPlayerView.setLogLevel(mWZNetworkLogLevel);

            // Set the player's pre-buffer duration as stored dataInputStream the app prefs
            float preBufferDuration = GoCoderSDKPrefs.getPreBufferDuration(PreferenceManager.getDefaultSharedPreferences(this));

            mStreamPlayerConfig.setPreRollBufferDuration(preBufferDuration);

            Log.d(TAGgg, "playStream:no exist ");
            mStreamPlayerConfig.setIsPlayback(true);
            mStreamPlayerConfig.setHostAddress(sourceConnectionInformation.getPrimaryServer());
            mStreamPlayerConfig.setApplicationName(sourceConnectionInformation.getApplication());
            mStreamPlayerConfig.setStreamName(sourceConnectionInformation.getStreamName());
            mStreamPlayerConfig.setPortNumber(1935);
            mStreamPlayerConfig.setUsername(sourceConnectionInformation.getUsername());
            mStreamPlayerConfig.setPassword(sourceConnectionInformation.getPassword());

            mStreamPlayerConfig.setAudioEnabled(true);
            mStreamPlayerConfig.setVideoEnabled(true);
            // Start playback of the live stream
            mStreamPlayerView.play(mStreamPlayerConfig, this);
        }
    }

    /**
     * Click handler for the playback button
     */
    public void onTogglePlayStream() {
        //다이어로그 띄우는 거 없이 나가기
        if (mStreamPlayerView.isPlaying()) {
            Log.d(TAGgg, "onTogglePlayStream:mStreamPlayerView.isPlaying() true ");
            showTearingdownDialog();
            mStreamPlayerView.stop();
            mStreamPlayerView.getCurrentStatus().waitForState(WOWZState.IDLE);
        } else if (mStreamPlayerView.isReadyToPlay()) {
            Log.d(TAGgg, "onTogglePlayStream:mStreamPlayerView.isPlaying() false, isreadytoplay true ");
            WOWZLog.debug("onTogglePlayStream() :: DECODER STATUS :: start stream");
            this.playStream();
        } else {
            Log.d(TAGgg, "onTogglePlayStream:mStreamPlayerView.isPlaying() false, isreadyplay false ");
        }
    }

    /**
     * WOWZStatusCallback interface methods
     */
    @Override
    public synchronized void onWZStatus(WOWZStatus status) {
        final WOWZStatus playerStatus = new WOWZStatus(status);

        new Handler(Looper.getMainLooper()).post(() -> {
            WOWZLog.debug("DECODER STATUS: 000 [player activity] current: " + playerStatus.toString());
            switch (playerStatus.getState()) {
                case WOWZPlayerView.STATE_PLAYING:
                    Log.d(TAGgg, "onWZStatus:STATE_PLAYING ");
                    // Keep the screen on while we are playing back the stream
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    /*if (mStreamPlayerConfig.getPreRollBufferDuration() == 0f) {
                        mTimerView.startTimer();
                    }
*/
                    // Since we have successfully opened up the server connection, store the connection info for auto complete
                    GoCoderSDKPrefs.storeHostConfig(PreferenceManager.getDefaultSharedPreferences(PlayerActivity.this), mStreamPlayerConfig);

                    // Log the stream metadata
                    WOWZLog.debug(TAG, "Stream metadata:\n" + mStreamPlayerView.getMetadata());
                    break;

                case WOWZPlayerView.STATE_READY_TO_PLAY:
                    Log.d(TAGgg, "onWZStatus:STATE_READY_TO_PLAY: ");
                    //Toast.makeText(this, "STATE_READY_TO_PLAY", Toast.LENGTH_SHORT).show();
                    // Clear the "keep screen on" flag
                    WOWZLog.debug(TAG, "STATE_READY_TO_PLAY player activity status!");
                    if (playerStatus.getLastError() != null)

                        // displayErrorDialog(playerStatus.getLastError());
                        //   Toast.makeText(PlayerActivity.this, playerStatus.getLastError().toString(), Toast.LENGTH_SHORT).show();

                        playerStatus.clearLastError();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    //mTimerView.stopTimer();
                    if (GlobalPlayerStateManager.isReady()) {
                        hideTearingdownDialog();
                    }

                    break;

                case WOWZPlayerView.STATE_PREBUFFERING_STARTED:
                    Log.d(TAGgg, "onWZStatus:STATE_PREBUFFERING_STARTED: ");
                    //Toast.makeText(this, "STATE_PREBUFFERING_STARTED", Toast.LENGTH_SHORT).show();
                    WOWZLog.debug(TAG, "**** Decoder Dialog for buffering should show...");
                    showBuffering();
                    break;

                case WOWZPlayerView.STATE_PREBUFFERING_ENDED:
                    Log.d(TAGgg, "onWZStatus:STATE_PREBUFFERING_ENDED ");
                    WOWZLog.debug(TAG, "**** Decoder Dialog for buffering should stop...");
                    hideBuffering();
                    //Toast.makeText(this, "STATE_PREBUFFERING_ENDED ", Toast.LENGTH_SHORT).show();
                    // Make sure player wasn't signaled to shutdown
                    /*if (mStreamPlayerView.isPlaying()) {
                        mTimerView.startTimer();
                    }*/

                    break;

                case WOWZPlayerView.STATE_PLAYBACK_COMPLETE:
                    if (!mStreamPlayerView.isPlaying()) {
                        Log.d(TAGgg, "스트림 뷰가 작동 안해 ");
                        //사용자가 홈 버튼을 눌러 홈 화면을 갔다가 온 경우
                        if (justone == -1) {
                            return;
                        }

                        if (justone == 0) {
                            Log.d(TAGgg, "STATE_PLAYBACK_COMPLETE" + justone + "");
                            //나간다는 것 서버에도 알려주기
                            type = "exit";
                            message = "exit";
                            //나간다는 것 서버에도 알려주기
                            new Sendmessage().execute();
                            goback();

                        } else {
                            Log.d(TAGgg, "onWZStatus: " + "STATE_PLAYBACK_COMPLETE" + justone + "exit");
                            //나간다는 것 서버에도 알려주기
                            type = "exit";
                            message = "exit";
                            new Sendmessage().execute();
                            goback();
                        }

                    } else {
                        Log.d(TAGgg, "STATE_PLAYBACK_COMPLETE" + "onWZStatus: 스트림 뷰가 작동해");
                    }

                    Log.d(TAGgg, "onWZStatus:STATE_PLAYBACK_COMPLETE: ");
                    // Toast.makeText(this, "STATE_PLAYBACK_COMPLETE", Toast.LENGTH_SHORT).show();
                    WOWZLog.debug("DECODER STATUS: [player activity2] current: " + playerStatus.toString());

                    break;
                case WOWZPlayerView.STATE_ERROR:
                    Log.d(TAGgg, "onWZStatus:STATE_ERROR ");
                    break;
                case WOWZPlayerView.STATE_STOPPING:
                    Log.d(TAGgg, "onWZStatus:STATE_STOPPING ");
                    break;
                case WOWZPlayerView.STATE_PREPARING:
                    Log.d(TAG, "onWZStatus:preparing ");
                    break;
                case WOWZState.PAUSED:
                    Log.d(TAG, "onWZStatus:paused ");
                    break;
                default:
                    Log.d(TAGgg, "onWZStatus:default ");
                    WOWZLog.debug("DECODER STATUS: [player activity] current: " + playerStatus.toString());
                    WOWZLog.debug("DECODER STATUS: [player activity] current: " + GlobalPlayerStateManager.isReady());
                    break;
            }
            syncUIControlState();
        });
    }

    private void goback() {

        if (clickgoout) {
            Intent intent = new Intent(PlayerActivity.this, StreamingActivity.class);
            intent.putExtra("method", "delete");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            Log.d(TAG, "goback:true click finish ");
            finish();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
            builder.setTitle("방송 종료");
            builder.setMessage("사용자가 방송을 종료했습니다. 확인을 누르시면 이전 화면으로 돌아갑니다.");
            builder.setPositiveButton("확인", (dialog, which) -> {
                justone = justone + 1;
                Intent intent = new Intent(PlayerActivity.this, StreamingActivity.class);
                intent.putExtra("method", "delete");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                Log.d(TAG, "goback:false click finish ");
                finish();
            });
            builder.show();
            /*Intent intent = new Intent(PlayerActivity.this, StreamingActivity.class);
            intent.putExtra("method", "delete");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();*/

        }
    }

    @Override
    public synchronized void onWZError(final WOWZStatus playerStatus) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(mtx, playerStatus.getLastError().toString(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onWZError: " + playerStatus.getLastError());
            //Toast.makeText(PlayerActivity.this, playerStatus.getLastError().toString(), Toast.LENGTH_SHORT).show();
            //displayErrorDialog(playerStatus.getLastError());
            playerStatus.setState(WOWZState.IDLE);
            syncUIControlState();
        });
    }

    /**
     * Click handler for the mic/mute button
     *//*
    public void onToggleMute(View v) {
        mBtnMic.toggleState();

        if (mStreamPlayerView != null)
            mStreamPlayerView.mute(!mBtnMic.isOn());

        mSeekVolume.setEnabled(mBtnMic.isOn());
    }

    public void onToggleScaleMode(View v) {
        int newScaleMode = mStreamPlayerView.getScaleMode() == WOWZMediaConfig.RESIZE_TO_ASPECT ? WOWZMediaConfig.FILL_VIEW : WOWZMediaConfig.RESIZE_TO_ASPECT;
        mBtnScale.setState(newScaleMode == WOWZMediaConfig.FILL_VIEW);
        mStreamPlayerView.setScaleMode(newScaleMode);
    }*/

    /**
     * Click handler for the metadata button
     */
    public void onStreamMetadata(View v) {
        WOWZDataMap streamMetadata = mStreamPlayerView.getMetadata();
        WOWZDataMap streamStats = mStreamPlayerView.getStreamStats();
        WOWZDataMap streamInfo = new WOWZDataMap();

        streamInfo.put("- Stream Statistics -", streamStats);
        streamInfo.put("- Stream Metadata -", streamMetadata);
        //streamInfo.put("- Stream Configuration -", streamConfig);

        DataTableFragment dataTableFragment = DataTableFragment.newInstance("Stream Information", streamInfo, false, false);

        // Display/hide the data table fragment
        getFragmentManager().beginTransaction()
                .add(android.R.id.content, dataTableFragment)
                .addToBackStack("metadata_fragment")
                .commit();
    }

    /**
     * Click handler for the settings button
     */
    public void onSettings(View v) {
        // Display the prefs fragment
        GoCoderSDKPrefs.PrefsFragment prefsFragment = new GoCoderSDKPrefs.PrefsFragment();
        prefsFragment.setFixedSource(true);
        prefsFragment.setForPlayback(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, prefsFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Update the state of the UI controls
     */
    private void syncUIControlState() {
        boolean disableControls = !(GlobalPlayerStateManager.isReady() || mStreamPlayerView.isPlaying()); // (!(GlobalPlayerStateManager.isReady() ||  mStreamPlayerView.isReadyToPlay() || mStreamPlayerView.isPlaying()) || sGoCoderSDK == null);
        if (disableControls) {
            Log.d(TAG, "syncUIControlState: ");
            mBtnPlayStream.setEnabled(false);
            mBtnSettings.setEnabled(false);
            //     mSeekVolume.setEnabled(false);
            //   mBtnScale.setEnabled(false);
            //mBtnMic.setEnabled(false);
            // mStreamMetadata.setEnabled(false);
        } else {
            mBtnPlayStream.setState(mStreamPlayerView.isPlaying());
            mBtnPlayStream.setEnabled(true);
            if (mStreamPlayerConfig.isAudioEnabled()) {
                //  mBtnMic.setVisibility(View.VISIBLE);
//                mBtnMic.setEnabled(true);

                //   mSeekVolume.setVisibility(View.VISIBLE);
                // mSeekVolume.setEnabled(mBtnMic.isOn());
                // mSeekVolume.setProgress(mStreamPlayerView.getVolume());
            } else {
                // mSeekVolume.setVisibility(View.GONE);
                //              mBtnMic.setVisibility(View.GONE);
            }

         /*   mBtnScale.setVisibility(View.VISIBLE);
            mBtnScale.setVisibility(mStreamPlayerView.isPlaying() && mStreamPlayerConfig.isVideoEnabled() ? View.VISIBLE : View.GONE);
            mBtnScale.setEnabled(mStreamPlayerView.isPlaying() && mStreamPlayerConfig.isVideoEnabled());
*/
            mBtnSettings.setEnabled(!mStreamPlayerView.isPlaying());
            mBtnSettings.setVisibility(mStreamPlayerView.isPlaying() ? View.GONE : View.VISIBLE);

            //          mStreamMetadata.setEnabled(mStreamPlayerView.isPlaying());
            //        mStreamMetadata.setVisibility(mStreamPlayerView.isPlaying() ? View.VISIBLE : View.GONE);
        }
    }

    private void showTearingdownDialog() {

        try {
            if (mGoingDownDialog == null) return;
            hideBuffering();
            if (!mGoingDownDialog.isShowing()) {
                mGoingDownDialog.setCancelable(false);
                mGoingDownDialog.show();
            }
        } catch (Exception ex) {
            WOWZLog.warn(TAG, "showTearingdownDialog:" + ex);
        }
    }

    private void hideTearingdownDialog() {

        try {
            if (mGoingDownDialog == null) return;
            hideBuffering();
            mGoingDownDialog.dismiss();
        } catch (Exception ex) {
            WOWZLog.warn(TAG, "hideTearingdownDialog exception:" + ex);
        }
    }

    private void showBuffering() {
        try {
            if (mBufferingDialog == null) return;

            final Handler mainThreadHandler = new Handler(getBaseContext().getMainLooper());
            mBufferingDialog.setCancelable(false);
            mBufferingDialog.show();
            mBufferingDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
            (new Thread() {
                public void run() {
                    try {
                        int cnt = 0;
                        while (GlobalPlayerStateManager.DECODER_VIDEO_STATE != WOWZState.STARTING && GlobalPlayerStateManager.DECODER_VIDEO_STATE != WOWZState.RUNNING) {
                            if (cnt > 3) {
                                break;
                            }
                            Thread.sleep(1000);
                            cnt++;
                        }
                    } catch (Exception ex) {
                        WOWZLog.warn(TAG, "showBuffering:" + ex);
                    }
                    mainThreadHandler.post(() -> mBufferingDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true));
                }
            }).start();
        } catch (Exception ex) {
            WOWZLog.warn(TAG, "showBuffering:" + ex);
        }
    }

    private void cancelBuffering() {
        Log.d("ss", "cancelBuffering: ");
        showTearingdownDialog();
        if (mStreamPlayerConfig.getHLSBackupURL() != null || mStreamPlayerConfig.isHLSEnabled() ||
                (mStreamPlayerView != null && mStreamPlayerView.isPlaying())) {
            Log.d("ss", "cancelBuffering:stop +");
            mStreamPlayerView.stop();
        } else {
            hideTearingdownDialog();
        }
    }

    private void hideBuffering() {
        if (!mStreamPlayerView.isPlaying()) {
            Log.d("끝내", "플레이어 작동 안돼 버퍼에서?>: ");
            if (bufferone == 0) {
                Log.d("끝내자", bufferone + "");
                bufferone = bufferone + 1;

            } else {
                Log.d("끝내자", justone + "onWZStatus: 이미 한번 끝냈어");

            }
        } else {
            Log.d("끝내", "플레이어 작동 됌 버퍼에서: ");
        }
        Log.d(TAG, "hideBuffering: ");
        if (mBufferingDialog.isShowing())
            mBufferingDialog.dismiss();
    }

    @Override
    public void syncPreferences() {
        Log.d(TAG, "syncPreferences: ");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mWZNetworkLogLevel = Integer.valueOf(prefs.getString("wz_debug_net_log_level", String.valueOf(WOWZLog.LOG_LEVEL_DEBUG)));

        mStreamPlayerConfig.setIsPlayback(true);
        if (mStreamPlayerConfig != null)
            GoCoderSDKPrefs.updateConfigFromPrefsForPlayer(prefs, mStreamPlayerConfig);
    }

}
