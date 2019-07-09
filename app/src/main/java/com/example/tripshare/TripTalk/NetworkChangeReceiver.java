package com.example.tripshare.TripTalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
       /* String status = NetworkUtill.getConnectivityStatusString(context);
        Toast.makeText(context, status, Toast.LENGTH_LONG).show();*/
       /* if (m_OnChangeNetworkStatusListener == null) {
            return;
        }
*/
        Intent mIntent = new Intent("network_changed");
        context.sendBroadcast(mIntent);

        String strAction = intent.getAction();

        if(strAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            Toast.makeText(context, "와이파이 4g있긴함 됨", Toast.LENGTH_SHORT).show();
        }

          /*  if (strAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            try {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                NetworkInfo _wifi_network =
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (_wifi_network != null) {
                    // wifi, 3g 둘 중 하나라도 있을 경우
                    if (_wifi_network != null && activeNetInfo != null) {
                        Toast.makeText(activity, "와이파이 4g있긴함 됨", Toast.LENGTH_SHORT).show();
                    }
                }
                // wifi, 3g 둘 다 없을 경우
                else {
                    Toast.makeText(activity, "연결 안됨", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.d("ULNetworkReceiver", e.getMessage());
            }*/
            /*if (strAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            switch (m_WifiManager.getWifiState()) {
                case WifiManager.WIFI_STATE_DISABLED:
                    m_OnChangeNetworkStatusListener.OnChanged(WIFI_STATE_DISABLED);
                    break;

                case WifiManager.WIFI_STATE_DISABLING:
                    m_OnChangeNetworkStatusListener.OnChanged(WIFI_STATE_DISABLING);
                    break;

                case WifiManager.WIFI_STATE_ENABLED:
                    m_OnChangeNetworkStatusListener.OnChanged(WIFI_STATE_ENABLED);
                    break;

                case WifiManager.WIFI_STATE_ENABLING:
                    m_OnChangeNetworkStatusListener.OnChanged(WIFI_STATE_ENABLING);
                    break;

                case WifiManager.WIFI_STATE_UNKNOWN:
                    m_OnChangeNetworkStatusListener.OnChanged(WIFI_STATE_UNKNOWN);
                    break;
            }
        } else if (strAction.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo networkInfo = m_ConnManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if ((networkInfo != null) && (networkInfo.isAvailable() == true)) {

                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    m_OnChangeNetworkStatusListener.OnChanged(NETWORK_STATE_CONNECTED);

                } else if (networkInfo.getState() == NetworkInfo.State.CONNECTING) {
                    m_OnChangeNetworkStatusListener.OnChanged(NETWORK_STATE_CONNECTING);

                } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    m_OnChangeNetworkStatusListener.OnChanged(NETWORK_STATE_DISCONNECTED);

                } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTING) {
                    m_OnChangeNetworkStatusListener.OnChanged(NETWORK_STATE_DISCONNECTING);

                } else if (networkInfo.getState() == NetworkInfo.State.SUSPENDED) {
                    m_OnChangeNetworkStatusListener.OnChanged(NETWORK_STATE_SUSPENDED);

                } else if (networkInfo.getState() == NetworkInfo.State.UNKNOWN) {
                    m_OnChangeNetworkStatusListener.OnChanged(NETWORK_STATE_UNKNOWN);
                }
            }
        }*/

        }
    }

