package com.example.tripshare.LoginRegister;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.tripshare.R;

/*

자동로그인과 사용자의 이름을 다른 화면에서 보여주기 위해
쉐어드에 저장하고 꺼내는 메소드 들이 모인 클래스


*/

public class PrefConfig {

    private static final String TAG ="pref" ;
    private SharedPreferences sharedPreferences;
    private Context context;
    private static final String KEY_ACCESS_TOKEN = "token";

    public  PrefConfig(Context context){
        this.context = context;
        Log.d(TAG, "PrefConfig: "+context.getString(R.string.pref_userinfo));
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.pref_userinfo), Context.MODE_PRIVATE);
    }


    //자동로그인을 위해
    //사용자가 로그인 했을 경우 true값을 쉐어드에 저장
    //로그인을 안했거나 로그아웃을 했으면 false를 저장
    public void writeLoginStatus(boolean status){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.pref_login_status), status);
        editor.commit();
    }

    //자동로그인을 위해
    //로그인을 했더라면 true 아니면 false를 반환
    public boolean readLoginStatus(){
        return sharedPreferences.getBoolean(context.getString(R.string.pref_login_status), false);
    }

    //로그인 했을 경우 다른 화면에서도 로그인을 유지하기 위해
    //로그인한 사용자의 이름을 쉐어드에 저장한다.
    public void writeEmail(String email){
        Log.d(TAG, "writeEmail: email-"+email);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_user_email),email);
        Log.d(TAG, "writeEmail: email-"+sharedPreferences.getString(context.getString(R.string.pref_user_email),"oo"));
        editor.commit();
    }
    //로그인한 사용자의 이름을 읽는다.
    public String readEmail(){
        return sharedPreferences.getString(context.getString(R.string.pref_user_email), "User");
    }

    //로그인 종류 저장하기
    public void writelogmethod(String method){
        Log.d(TAG, "writelogmethod: method-"+method);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_log_method),method);
        Log.d(TAG, "writelogmethod: method-"+sharedPreferences.getString(context.getString(R.string.pref_log_method),"oo"));
        editor.commit();
    }
    //로그인인 방법을 알기
    public String readlogmethod(){
        return sharedPreferences.getString(context.getString(R.string.pref_log_method), "");
    }

    //로그인, 회원가입시 보여줄 메세지
    public void displayToast(String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    //로그인 할 때 발급받은 토큰을 저장하기
    public boolean storeToken(String token){
        Log.d(TAG, "storeToken:token "+token);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply();
        return true;
    }

    public String getToken(){
        Log.d(TAG, "getToken: "+sharedPreferences.getString(KEY_ACCESS_TOKEN, null));
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public void writename(String name){
        Log.d(TAG, "writename: "+name);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_user_name),name);
        editor.apply();
        Log.d(TAG, "writename:chech save "+sharedPreferences.getString(context.getString(R.string.pref_user_name),""));
    }

    public String getName(){
        Log.d(TAG, "getName: "+sharedPreferences.getString(context.getString(R.string.pref_user_name),""));
        return sharedPreferences.getString(context.getString(R.string.pref_user_name),"");
    }
    public void writeimgurl(String imgurl){
        Log.d(TAG, "writeurl: "+imgurl);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_user_imgurl),imgurl);
        editor.apply();
        Log.d(TAG, "writeurl:check save "+sharedPreferences.getString(context.getString(R.string.pref_user_imgurl),""));
    }

    public String readimgurl(){
        Log.d(TAG, "geturl: "+sharedPreferences.getString(context.getString(R.string.pref_user_imgurl),""));
        return sharedPreferences.getString(context.getString(R.string.pref_user_imgurl),"");
    }

    public void writewallet(String walletpath){
        Log.d(TAG, "writewallet: "+walletpath);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("walletfilepath",walletpath);
        editor.apply();
        Log.d(TAG, "writewallet:check save "+sharedPreferences.getString("walletfilepath",""));
    }

    public String readwallet(){
        Log.d(TAG, "walletfilepath: "+sharedPreferences.getString("walletfilepath",""));
        return sharedPreferences.getString("walletfilepath","");
    }

    public void writeqrimg(String qrimg){
        Log.d(TAG, "writeqrimg: "+qrimg);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("qrimg",qrimg);
        editor.apply();
        Log.d(TAG, "writeqrimg:check save "+sharedPreferences.getString("qrimg",""));
    }

    public String readqrimg(){
        Log.d(TAG, "readqrimg: "+sharedPreferences.getString("qrimg",""));
        return sharedPreferences.getString("qrimg","");
    }


}
