/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.example.tripshare.androidwebrtc.call;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.R;
import com.example.tripshare.TripTalk.TalkService;
import com.example.tripshare.androidwebrtc.web_rtc.AppRTCAudioManager;
import com.example.tripshare.androidwebrtc.web_rtc.AppRTCClient;
import com.example.tripshare.androidwebrtc.web_rtc.PeerConnectionClient;
import com.example.tripshare.androidwebrtc.web_rtc.WebSocketRTCClient;
import com.example.tripshare.databinding.ActivityCallBinding;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.example.tripshare.androidwebrtc.util.Constants.CAPTURE_PERMISSION_REQUEST_CODE;
import static com.example.tripshare.androidwebrtc.util.Constants.EXTRA_ROOMID;
import static com.example.tripshare.androidwebrtc.util.Constants.LOCAL_HEIGHT_CONNECTED;
import static com.example.tripshare.androidwebrtc.util.Constants.LOCAL_HEIGHT_CONNECTING;
import static com.example.tripshare.androidwebrtc.util.Constants.LOCAL_WIDTH_CONNECTED;
import static com.example.tripshare.androidwebrtc.util.Constants.LOCAL_WIDTH_CONNECTING;
import static com.example.tripshare.androidwebrtc.util.Constants.LOCAL_X_CONNECTED;
import static com.example.tripshare.androidwebrtc.util.Constants.LOCAL_X_CONNECTING;
import static com.example.tripshare.androidwebrtc.util.Constants.LOCAL_Y_CONNECTED;
import static com.example.tripshare.androidwebrtc.util.Constants.LOCAL_Y_CONNECTING;
import static com.example.tripshare.androidwebrtc.util.Constants.REMOTE_HEIGHT;
import static com.example.tripshare.androidwebrtc.util.Constants.REMOTE_WIDTH;
import static com.example.tripshare.androidwebrtc.util.Constants.REMOTE_X;
import static com.example.tripshare.androidwebrtc.util.Constants.REMOTE_Y;
import static com.example.tripshare.androidwebrtc.util.Constants.STAT_CALLBACK_PERIOD;
import static org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL;
import static org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FIT;


/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
public class CallActivity extends AppCompatActivity
        implements AppRTCClient.SignalingEvents, PeerConnectionClient.PeerConnectionEvents, com.example.tripshare.androidwebrtc.call.OnCallEvents {
    private static final String LOG_TAG = "CallActivity";
    private static final String TAG = "CallActivity";

    private PeerConnectionClient peerConnectionClient;
    private AppRTCClient appRtcClient;
    private AppRTCClient.SignalingParameters signalingParameters;
    private AppRTCAudioManager audioManager;
    private EglBase rootEglBase;
    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<>();
    private Toast logToast;
    private boolean activityRunning;

    private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;

    private boolean iceConnected;
    private boolean isError;
    private long callStartedTimeMs;
    private boolean micEnabled = true;
    String videoroomstr, ymd, hm;
    private boolean mBound;
    private Messenger mMessenger, mService;
    private String youremail,myname, myemail, myurl;
    private PrefConfig prefConfig;

    private ActivityCallBinding binding;

    class gooutvideocallHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TalkService.IMG_SET_RNUM:
                    Log.d(TAG, "handleMessage:요청 거절 ");
                    Log.d(TAG, "handleMessage:거절한 사람이름 " + msg.obj);
                    Toast.makeText(CallActivity.this, msg.obj + "님이 영상 통화를 취소했습니다.", Toast.LENGTH_SHORT).show();
                    disconnect();
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

            mMessenger = new Messenger(new gooutvideocallHandler());
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

                //방 번호를 서비스에게 보낸다. 나중에 메세지가 왔을 때 메세지의 방번호가 일치하면 메세지를 준다.
                Log.d(TAG, "onServiceConnected: set " + TalkService.MSG_SET_RNUM);
                msg = android.os.Message.obtain(null, TalkService.MSG_SET_RNUM, videoroomstr);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefConfig = new PrefConfig(this);
        myemail = prefConfig.readEmail();
        myname = prefConfig.getName();
        myurl = prefConfig.readimgurl();
        // keyguard는 잠금화면을 의미한다.
        //1. 잠금화면을 풀어야 화면을 보여준다는 것, api 26에서는 사용 안된다.
        //2. 잠금화면이어도 화면을 보여줌
        //3. 화면을 on시킨다.LayoutParams.FLAG_DISMISS_KEYGUARD |
        getWindow().addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED | LayoutParams.FLAG_TURN_SCREEN_ON);
        //결론 상단 status bar도 하단의 soft key도 안 보인다.
        //1. 기기 네비게이션 바를 숨긴다.
        //2. 전체화면을 사용한다.
        //3. min api 19이상에서 가능하다.
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call);

        //상대방 화면 index 0에 넣어준다.
        remoteRenderers.add(binding.remoteVideoView);

        // Create video renderers.
        rootEglBase = EglBase.create();
        binding.localVideoView.init(rootEglBase.getEglBaseContext(), null);
        binding.remoteVideoView.init(rootEglBase.getEglBaseContext(), null);

        //내 화면을 맨 위로 배치한다.
        //해상도를 특정 버퍼(??)고정하는 것을 가능하게 한다.
        binding.localVideoView.setZOrderMediaOverlay(true);
        binding.localVideoView.setEnableHardwareScaler(true);
        binding.remoteVideoView.setEnableHardwareScaler(true);
        //연결이 안됬으므로 2개 비디오 뷰 위치 조정
        updateVideoView();

        // Get Intent parameters.
        final Intent intent = getIntent();
        String roomId = intent.getStringExtra(EXTRA_ROOMID);
        Log.d(LOG_TAG, "Room ID: " + roomId);
        youremail = intent.getStringExtra("youremail");
        Log.d(TAG, "onCreate: youremail "+youremail);
        videoroomstr = roomId;
        //방번호가 없는 경우 기존 화면으로 돌아간다.
        if (roomId == null || roomId.length() == 0) {
            logAndToast(getString(R.string.missing_url));
            Log.e(LOG_TAG, "Incorrect room ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        //해상도 기본으로 설정
        // If capturing format is not specified for screencapture, use screen resolution.
        peerConnectionParameters = PeerConnectionClient.PeerConnectionParameters.createDefault();

        //상대방과 연결할 클라이언트 웹소켓 객체를 만듬
        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.
        appRtcClient = new WebSocketRTCClient(this);

        //서버에 연결해 영상통화할 방을 만든다.
        // Create connection parameters.
        roomConnectionParameters = new AppRTCClient.RoomConnectionParameters("https://appr.tc", roomId, false);

        //영상통화 중지, 촬영 방법 변경, 마이크 사용 등의 기능
        setupListeners();

        //peerconnection client 객체 생성
        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.createPeerConnectionFactory(this, peerConnectionParameters, this);

        startCall();
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


    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        Log.d(TAG, "onStop: ");

    }

    private void setupListeners() {
        //영상통화 중지
        binding.buttonCallDisconnect.setOnClickListener(view -> onCallHangUp());
        //카메라 전면 후면 바꾸는 것
        binding.buttonCallSwitchCamera.setOnClickListener(view -> onCameraSwitch());
        //마이크 버튼 투명도 결정
        binding.buttonCallToggleMic.setOnClickListener(view -> {
            boolean enabled = onToggleMic();
            //상대방 연결되면 마이크 버튼 완전 불투명,
            //연결 안되면 약간 투명
            binding.buttonCallToggleMic.setAlpha(enabled ? 1.0f : 0.3f);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE) {
            return;
        }
        startCall();
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }

    private boolean captureToTexture() {
        return true;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        Log.d(TAG, "createCameraCapturer:size " + deviceNames.length);
        for (int i = 0; i < deviceNames.length; i++) {
            Log.d(TAG, "createCameraCapturer: " + deviceNames[i]);
        }
        // First, try to find front facing camera
        Log.d(LOG_TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            Log.d(TAG, "createCameraCapturer: " + deviceName);
            if (enumerator.isFrontFacing(deviceName)) {
                Log.d(LOG_TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    Log.d(TAG, "createCameraCapturer:카메라 발견 " + videoCapturer.toString());
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Log.d(LOG_TAG, "Looking for others cameras.");
        for (String deviceName : deviceNames) {
            Log.d(TAG, "createCameraCapturer:others " + deviceName);
            if (!enumerator.isFrontFacing(deviceName)) {
                Log.d(LOG_TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    Log.d(TAG, "createCameraCapturer:카메라 발견 " + videoCapturer.toString());
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    // Activity interfaces
    @Override
    public void onPause() {
        super.onPause();
        activityRunning = false;
        // Don't stop the video when using screencapture to allow user to show other apps to the remote
        // end.
        if (peerConnectionClient != null) {
            peerConnectionClient.stopVideoSource();
        }

        //
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
    }

    @Override
    public void onResume() {
        super.onResume();
        activityRunning = true;
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null) {
            peerConnectionClient.startVideoSource();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Bind to the service
        bindService(new Intent(this, TalkService.class), mConnection,
                Context.BIND_AUTO_CREATE);
        Bundle args = getIntent().getExtras();
        if (args != null) {
            String contactName = args.getString(EXTRA_ROOMID);
            //binding.contactNameCall.setText(contactName);
        }
        binding.captureFormatTextCall.setVisibility(View.GONE);
        binding.captureFormatSliderCall.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        disconnect();
        if (logToast != null) {

            logToast.cancel();
        }
        activityRunning = false;
        rootEglBase.release();
        super.onDestroy();
    }

    // CallFragment.OnCallEvents interface implementation.
    @Override
    public void onCallHangUp() {
        //통화 취소 시간
        getymdhm();
        //통화 취소 요청을 보냄
        new CancelVideoSender().execute();
        //disconnect();
    }

    @Override
    public void onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
        }
    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        if (peerConnectionClient != null) {
            peerConnectionClient.changeCaptureFormat(width, height, framerate);
        }
    }

    @Override
    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }
        return micEnabled;
    }

    private void updateVideoView() {
        // 2. 상대방이 연결 됬을 때
        //상대방 화면을 위로
        //내 화면의 위치를 아래쪽으로 지정함
        binding.remoteVideoLayout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
        binding.remoteVideoView.setScalingType(SCALE_ASPECT_FILL);
        //상대방 화면 거울모드 비활성화
        binding.remoteVideoView.setMirror(false);

        if (iceConnected) {
            binding.localVideoLayout.setPosition(
                    LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
            binding.localVideoView.setScalingType(SCALE_ASPECT_FIT);
        } else {
            binding.localVideoLayout.setPosition(
                    LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING);
            binding.localVideoView.setScalingType(SCALE_ASPECT_FILL);
        }
        //내 화면은 거울 모드 활성화
        binding.localVideoView.setMirror(true);
        //해당 뷰에 변경사항을 요청함
        binding.localVideoView.requestLayout();
        binding.remoteVideoView.requestLayout();
    }

    private void startCall() {
        //현재 시간
        callStartedTimeMs = System.currentTimeMillis();

        // Start room connection.
        Log.d(TAG, "startCall: " + getString(R.string.connecting_to, roomConnectionParameters.roomUrl));
        //logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl));

        //채팅방에 연결
        appRtcClient.connectToRoom(roomConnectionParameters);

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(this);
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(LOG_TAG, "Starting the audio manager...");
        //스피커 폰과 해드셋으로 소리 전달 장치가 변경 될 때마다 호출됨
        audioManager.start(this::onAudioManagerDevicesChanged);
    }

    // Should be called from UI thread
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(LOG_TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(LOG_TAG, "Call is connected in closed or error state");
            return;
        }
        //상대방이 연결이 됐을 때
        //내 화면을 아래로 상대방 화면을 위로 조정한다.
        updateVideoView();
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
    }

    //스피커 폰과 해드셋으로 소리 전달 장치가 변경 될 때마다 호출됨
    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(LOG_TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect() {
        activityRunning = false;
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        binding.localVideoView.release();
        binding.remoteVideoView.release();
        /*if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }*/
        if (iceConnected && !isError) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (!activityRunning) {

            Log.e(LOG_TAG, "Critical error: " + errorMessage);
            disconnect();
        } else {
            Log.d(TAG, "disconnectWithErrorMessage: " + errorMessage);
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.channel_error_title))
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton(R.string.ok,
                            (dialog, id) -> {
                                dialog.cancel();
                                disconnect();
                            })
                    .create()
                    .show();
        }
    }

    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {
        Log.d(LOG_TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        Log.d(TAG, "logAndToast: " + msg);
        if (msg.equals("Room response ")) {

        }
       // logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
       // logToast.show();
    }

    private void reportError(final String description) {
        runOnUiThread(() -> {
            if (!isError) {
                isError = true;
                disconnectWithErrorMessage(description);
            }
        });
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        Log.d(TAG, "createVideoCapturer: " + useCamera2());
        if (useCamera2()) {
            Log.d(LOG_TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Log.d(LOG_TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            Log.d(TAG, "createVideoCapturer:Failed to open camera ");
            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private void onConnectedToRoomInternal(final AppRTCClient.SignalingParameters params) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.d(TAG, "onConnectedToRoomInternal:Creating peer connection, delay=" + delta + "ms ");
        signalingParameters = params;
        //logAndToast("Creating peer connection, delay=" + delta + "ms");


        //기기에 비디오가 이용 가능하다면
        VideoCapturer videoCapturer = null;
        Log.d(TAG, "onConnectedToRoomInternal: " + peerConnectionParameters.videoCallEnabled);
        if (peerConnectionParameters.videoCallEnabled) {
            //내 얼굴 잡을 비디오 생성
            videoCapturer = createVideoCapturer();
        }
        //상대방과 연결을 시도함
        peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(), binding.localVideoView,
                remoteRenderers, videoCapturer, signalingParameters);
        Log.d(TAG, "onConnectedToRoomInternal: " + signalingParameters.initiator);
        if (signalingParameters.initiator) {
            //전화 겨는 사람이라면
            Log.d(TAG, "Creating OFFER... ");
            //logAndToast("Creating OFFER...");
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            //전화 받는 사람이라면
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                Log.d(TAG, "Creating ANSWER...");
                //logAndToast("Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }

    //채팅방에 전화 건 사람이 연결되면
    @Override
    public void onConnectedToRoom(final AppRTCClient.SignalingParameters params) {
        runOnUiThread(() -> onConnectedToRoomInternal(params));
    }

    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                Log.e(LOG_TAG, "Received remote SDP for non-initilized peer connection.");
                return;
            }
            Log.d(TAG, "Received remote " + sdp.type + ", delay=" + delta + "ms");
            //logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
            peerConnectionClient.setRemoteDescription(sdp);
            if (!signalingParameters.initiator) {
                logAndToast("Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                Log.e(LOG_TAG, "Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(candidate);
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                Log.e(LOG_TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(candidates);
        });
    }

    @Override
    public void onChannelClose() {
        runOnUiThread(() -> {
            logAndToast("Remote end hung up; dropping PeerConnection");
            disconnect();
        });
    }

    @Override
    public void onChannelError(final String description) {
        reportError(description);
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(() -> {
            if (appRtcClient != null) {
                Log.d(TAG, "Sending " + sdp.type + ", delay=" + delta + "ms");
                //logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms");
                if (signalingParameters.initiator) {
                    appRtcClient.sendOfferSdp(sdp);
                } else {
                    appRtcClient.sendAnswerSdp(sdp);
                }
            }
            if (peerConnectionParameters.videoMaxBitrate > 0) {
                Log.d(LOG_TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(() -> {
            if (appRtcClient != null) {
                appRtcClient.sendLocalIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(() -> {
            if (appRtcClient != null) {
                appRtcClient.sendLocalIceCandidateRemovals(candidates);
            }
        });
    }

    //상대방이 연결되면?
    @Override
    public void onIceConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(() -> {
            Log.d(TAG, "onIceConnected: delay=" + delta + "ms");
            //logAndToast("ICE connected, delay=" + delta + "ms");
            iceConnected = true;
            callConnected();
        });
    }

    //상대방과의 연결이 해제 되면
    @Override
    public void onIceDisconnected() {
        runOnUiThread(() -> {
            // logAndToast("ICE disconnected");
            Log.d(TAG, "onIceDisconnected: ICE disconnected");
            iceConnected = false;
            disconnect();
        });
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        runOnUiThread(() -> {
        });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        reportError(description);
    }

    private class CancelVideoSender extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: hi i'm item_message_sender");

            //사용자 각각 알림에 나를 제외한 사용자를 넣기 위해
            String type = "mtext";
            String total = "1";
            String receiveremail = youremail;
            String message = "endvideocall";
            String rnum = videoroomstr;

            try {
                Log.d(TAG, "doInBackground: type " + type);
                Log.d(TAG, "doInBackground: total " + total);
                Log.d(TAG, "doInBackground: " + myemail);
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
            disconnect();
        }


    }
}
