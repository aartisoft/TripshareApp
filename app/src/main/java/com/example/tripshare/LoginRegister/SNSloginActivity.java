package com.example.tripshare.LoginRegister;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.MainActivity;
import com.example.tripshare.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SNSloginActivity extends AppCompatActivity {


    private static final String TAG = "SNS";
    private Button emlog, reg, fb;
    private Context mContext;
    private Button btn_custom_login;
    private LoginButton btn_kakao_login;
    private String password, email, image, name, method;
    SessionCallback callback;
    public static PrefConfig prefConfig;
    public static  ApiInterface apiInterface;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snslogin);

        //쉐어드 생성을 위해 반복 코드 줄이고
        prefConfig = new PrefConfig(this);
        //?? api 객체를 만든다.라고 생각하자.
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        emlog = findViewById(R.id.sns_log_bn);
        reg = findViewById(R.id.sns_reg_bn);

        //해당 기기의 현재 토큰이 firebase에 요청해서 받아오기
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {

            if (!task.isSuccessful()){
                //받기에 실패하면
                Log.d(TAG, "onCreate: "+task.getException());
                return;
            }
            token = task.getResult().getToken();
            Log.d(TAG, "onCreate: "+token);

        });

        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
        requestMe();

        reg.setOnClickListener(v -> {
            Intent intent = new Intent(SNSloginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        //이메일 로그인으로
        emlog.setOnClickListener(v -> {
            Intent intent = new Intent(SNSloginActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    public void requestMe() {
        //유저의 정보를 받아오는 함수
        UserManagement.getInstance().requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.e(TAG, "error message=" + errorResult);
//                super.onFailure(errorResult);
            }
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.d(TAG, "onSessionClosed1 =" + errorResult);
            }
            @Override
            public void onNotSignedUp() {
                //카카오톡 회원이 아닐시
                Log.d(TAG, "onNotSignedUp ");
            }
            @Override
            public void onSuccess(UserProfile result) {
                Log.e("UserProfile", result.toString());
                Log.e("UserProfile", result.getId() + "");
                Log.d(TAG, "onCreate:id"+result.getId());
                Log.d(TAG, "onCreate:id"+result.getNickname());
                Log.d(TAG, "onCreate:id"+result.getProfileImagePath());
                Log.d(TAG, "onCreate:id"+result.getThumbnailImagePath());
                Log.d(TAG, "onCreate:id"+result.getEmail());
            }
        });
    }
    class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {

            UserManagement.getInstance().requestMe(new MeResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    String message = "failed to get user info. msg=" + errorResult;

                    ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                    if (result == ErrorCode.CLIENT_ERROR_CODE) {
                        //에러로 인한 로그인 실패
                        // finish();
                    } else {
                        //redirectMainActivity();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                }

                @Override
                public void onNotSignedUp() {

                }

                @Override
                public void onSuccess(UserProfile userProfile) {
                    //로그인에 성공하면 로그인한 사용자의 일련번호, 닉네임, 이미지url등을 리턴합니다.
                    //사용자 ID는 보안상의 문제로 제공하지 않고 일련번호는 제공합니다.
                     email = userProfile.getEmail();
                     long id= userProfile.getId();
                     password = String.valueOf(id);
                     name = userProfile.getNickname();
                     image = userProfile.getProfileImagePath();
                     method="kakao";

                    Log.d(TAG, "onSuccess:id"+userProfile.getId());
                    Log.d(TAG, "onSuccess:id"+userProfile.getNickname());
                    Log.d(TAG, "onSuccess:id"+userProfile.getProfileImagePath());
                    Log.d(TAG, "onSuccess:id"+userProfile.getThumbnailImagePath());
                    // Log.e("UserProfile", userProfile.toString());
                    // Log.e("UserProfile", userProfile.getId() + "");
                    Log.d(TAG, "onSuccess:id"+userProfile.getEmail());

                    //서버에 데이터 전달
                    Call<User> call = apiInterface.performSNSLogin(email, name, password,image,token);
                    call.enqueue(new Callback<User>() {
                                     @Override
                                     public void onResponse(Call<User> call, Response<User> response) {
                                         if (response.isSuccessful()){
                                             User user = response.body();
                                             if (user.getResponse().equals("ok")) {
                                                 //자동로그인과 이메일 쉐어드에 저장
                                                 Log.d(TAG, "onResponse: "+user.getEmail());
                                                 prefConfig.writelogmethod(method);
                                                 prefConfig.writeEmail(email);
                                                 prefConfig.displayToast(name + "님 환영합니다^");
                                                 prefConfig.writeLoginStatus(true);
                                                 Intent intent = new Intent(SNSloginActivity.this, MainActivity.class);
                                                 startActivity(intent);

                                             }else{
                                                 Log.d(TAG, "onResponse: "+user.getResponse());
                                             }
                                         }else{
                                             prefConfig.displayToast("서버 연결에 문제가 있습니다.");

                                         }
                                     }

                                     @Override
                                     public void onFailure(Call<User> call, Throwable t) {
                                         prefConfig.displayToast("서버 연결에 문제가 있습니다."+t);
                                     }
                                 });
                }
            });

        }
        // 세션 실패시
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
        }
    }
}
