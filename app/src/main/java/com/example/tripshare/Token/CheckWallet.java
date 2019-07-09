package com.example.tripshare.Token;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.tripshare.R;

public class CheckWallet extends AppCompatActivity {

    private static final String TAG = "CheckWallet";
    WebView webView;
    WebSettings webSettings;
    String myaddress;
    String url = "https://ropsten.etherscan.io/address/";
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_wallet);

        //내 지갑 주소 받기
        if (getIntent().getStringExtra("address") != null){
            myaddress = getIntent().getStringExtra("address");
        }
        String loadurl = url+myaddress;
        Log.d(TAG, "onCreate:url "+loadurl);


        //웹뷰 세팅팅
        webView = findViewById(R.id.transaction_webview);
        webView.setWebViewClient(new WebViewClient());// 웹뷰에서 클릭했을 때 현재 페이지에서 이동하기, 새로운 페이지 안 뜨게
        webSettings = webView.getSettings(); // 세부 세팅 등록??
        webSettings.setJavaScriptEnabled(true); //자바스크립트 사용 허용
        webSettings.setLoadWithOverviewMode(true);
        webView.loadUrl(loadurl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
            //사용자가 뒤로가기 버튼을 눌렀고
            //뒤로갈 페이지가 있다면
            webView.goBack();
            return true;
        }
        //뒤로갈 페이지가 없다면 이전 화면으로 간다.
        return super.onKeyDown(keyCode, event);
    }
}
