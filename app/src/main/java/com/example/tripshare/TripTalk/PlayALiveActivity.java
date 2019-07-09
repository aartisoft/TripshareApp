package com.example.tripshare.TripTalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.tripshare.LiveStream.GoCoderSDKActivityBase;
import com.example.tripshare.LiveStream.StreamingActivity;
import com.example.tripshare.LiveStream.ui.VolumeChangeObserver;
import com.example.tripshare.R;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.data.WOWZDataMap;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.logging.WOWZLog;
import com.wowza.gocoder.sdk.api.player.GlobalPlayerStateManager;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

public class PlayALiveActivity extends GoCoderSDKActivityBase {
    WOWZPlayerView mStreamPlayerView;
    private static final String TAG = "PlayALiveActivity";

    WOWZPlayerConfig mStreamPlayerConfig;
    // The top-level GoCoder API interface

    private ProgressDialog mBufferingDialog = null;
    private ProgressDialog mGoingDownDialog = null;
    private WowzaGoCoder goCoder;
    int justone, bufferone;
    private VolumeChangeObserver mVolumeSettingChangeObserver = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_alive);
        justone = 0;
        bufferone = 0;
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

        //player 명시
        mStreamPlayerView = (WOWZPlayerView) findViewById(R.id.vwStreamPlayer);

        mStreamPlayerConfig = new WOWZPlayerConfig();
        mStreamPlayerConfig.setIsPlayback(true);
        mStreamPlayerConfig.setHostAddress("b95863.entrypoint.cloud.wowza.com");
        mStreamPlayerConfig.setApplicationName("app-fbf1");
        mStreamPlayerConfig.setStreamName("ed819160");
        mStreamPlayerConfig.setPortNumber(1935);
        mStreamPlayerConfig.setUsername("client42837");
        mStreamPlayerConfig.setPassword("fd1ceea2");

        mStreamPlayerConfig.setAudioEnabled(true);
        mStreamPlayerConfig.setVideoEnabled(true);

        if (sGoCoderSDK != null) {

            /*
            Packet change listener setup
             */
            final PlayALiveActivity activity = this;
            WOWZPlayerView.PacketThresholdChangeListener packetChangeListener = new WOWZPlayerView.PacketThresholdChangeListener() {
                @Override
                public void packetsBelowMinimumThreshold(int packetCount) {
                    WOWZLog.debug("Packets have fallen below threshold " + packetCount + "... ");

               }

                @Override
                public void packetsAboveMinimumThreshold(int packetCount) {
                    WOWZLog.debug("Packets have risen above threshold " + packetCount + " ... ");

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


                WOWZLog.debug("onWZDataEvent -> eventName " + eventName + " = " + meta);

                return null;
            });

            // testing player data event handler.
            mStreamPlayerView.registerDataEventListener("onStatus", (eventName, eventParams) -> {
                if (eventParams != null)
                    WOWZLog.debug("onWZDataEvent -> eventName " + eventName + " = " + eventParams.toString());

                return null;
            });

            // testing player data event handler.
            mStreamPlayerView.registerDataEventListener("onTextData", (eventName, eventParams) -> {
                if (eventParams != null)
                    WOWZLog.debug("onWZDataEvent -> " + eventName + " = " + eventParams.get("text"));

                return null;
            });
        }

        // WOWZMediaConfig.FILL_VIEW : WOWZMediaConfig.RESIZE_TO_ASPECT;
        mStreamPlayerView.setScaleMode(WOWZMediaConfig.FILL_VIEW);

        WOWZStatusCallback statusCallback = new StatusCallback();
        mStreamPlayerView.play(mStreamPlayerConfig, statusCallback);
    }
    @Override
    protected void onDestroy() {
        if (mVolumeSettingChangeObserver != null)
            getApplicationContext().getContentResolver().unregisterContentObserver(mVolumeSettingChangeObserver);

        super.onDestroy();
    }

    /**
     * Android Activity class methods
     */
    @Override
    protected void onResume() {
        super.onResume();

        justone =0;
        bufferone =0;
        hideBuffering();
        if (!GlobalPlayerStateManager.isReady()) {
            showTearingdownDialog();
            mStreamPlayerView.stop();
        }

        onTogglePlayStream();
    }

    private void onTogglePlayStream() {
        if (mStreamPlayerView.isPlaying()) {
            showTearingdownDialog();
            mStreamPlayerView.stop();
            mStreamPlayerView.getCurrentStatus().waitForState(WOWZState.IDLE);
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

    @Override
    protected void onPause() {


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


    /**
     * WOWZStatusCallback interface methods
     */
    @Override
    public synchronized void onWZStatus(WOWZStatus status) {
        final WOWZStatus playerStatus = new WOWZStatus(status);

        new Handler(Looper.getMainLooper()).post(() -> {
            WOWZLog.debug("DECODER STATUS: 000 [player activity] current: " + playerStatus.toString());
            Log.d(TAG, "onWZStatus:playerStatus.getState() == " + playerStatus.getState());
            switch (playerStatus.getState()) {
                case WOWZPlayerView.STATE_PLAYING:
                    // Keep the screen on while we are playing back the stream
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;

                case WOWZPlayerView.STATE_READY_TO_PLAY:
                    Toast.makeText(this, "STATE_READY_TO_PLAY", Toast.LENGTH_SHORT).show();
                    // Clear the "keep screen on" flag
                    if (playerStatus.getLastError() != null)

                        // displayErrorDialog(playerStatus.getLastError());
                    Toast.makeText(PlayALiveActivity.this, playerStatus.getLastError().toString(), Toast.LENGTH_SHORT).show();

                    playerStatus.clearLastError();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;

                case WOWZPlayerView.STATE_PREBUFFERING_STARTED:
                    Toast.makeText(this, "STATE_PREBUFFERING_STARTED", Toast.LENGTH_SHORT).show();
                    break;

                case WOWZPlayerView.STATE_PREBUFFERING_ENDED:
                    Toast.makeText(this, "STATE_PREBUFFERING_ENDED ", Toast.LENGTH_SHORT).show();

                    if (!mStreamPlayerView.isPlaying()) {
                        Log.d("끝내자", "스트림 뷰가 작동 안해 ");
                        if (justone == 0) {
                            Log.d("끝내자", justone + "");
                            justone = justone + 1;
                            Intent intent = new Intent(PlayALiveActivity.this, StreamingActivity.class);
                            intent.putExtra("method", "delete");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.d("끝내자", justone + "onWZStatus: 이미 한번 끝냈어");
                            Intent intent = new Intent(PlayALiveActivity.this, StreamingActivity.class);
                            intent.putExtra("method", "delete");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Log.d("끝내자", "onWZStatus: 스트림 뷰가 작동해");
                    }

                    break;

                case WOWZPlayerView.STATE_PLAYBACK_COMPLETE:
                    Toast.makeText(this, "STATE_PLAYBACK_COMPLETE", Toast.LENGTH_SHORT).show();
                    WOWZLog.debug("DECODER STATUS: [player activity2] current: " + playerStatus.toString());

                    break;
                default:
                    WOWZLog.debug("DECODER STATUS: [player activity] current: " + playerStatus.toString());
                    WOWZLog.debug("DECODER STATUS: [player activity] current: " + GlobalPlayerStateManager.isReady());
                    break;
            }
        });
    }

    @Override
    public synchronized void onWZError(final WOWZStatus playerStatus) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(PlayALiveActivity.this, playerStatus.getLastError().toString(), Toast.LENGTH_SHORT).show();
            //displayErrorDialog(playerStatus.getLastError());
            playerStatus.setState(WOWZState.IDLE);
        });
    }

}
