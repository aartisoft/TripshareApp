package com.example.tripshare.LoginRegister;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.MainActivity;
import com.example.tripshare.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "login";
    private TextView RegText;
    //interface를 위한 handler

    private EditText Email, Password;
    private Button LoginBn;
    public static PrefConfig prefConfig;
    public static  ApiInterface apiInterface;
    private static ImageView back;
    private String email, password;
    private String token;
    private LottieAnimationView animationView;
    private static final String URL_STORE_TOKEN = "http://115.71.238.81/fcm/storeFcmToken.php";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        animationView = (LottieAnimationView) findViewById(R.id.log_lottie);

        //쉐어드 생성을 위해 반복 코드 줄이고
        prefConfig = new PrefConfig(this);
        //?? api 객체를 만든다.라고 생각하자.
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);


        Email = findViewById(R.id.log_email_edit);
        Password = findViewById(R.id.log_pass_edit);
        LoginBn = findViewById(R.id.log_bn);
        back = findViewById(R.id.log_backImgView);

        //뒤로가기
        back.setOnClickListener(v -> finish());

        //로그인
        LoginBn.setOnClickListener(v -> performLogin());

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

    }


    private void performLogin(){
        //로딩 애니메이션 실행


        //사용자 입력값 받어
         email = Email.getText().toString();
         password = Password.getText().toString();
        Log.d(TAG, "performLogin: email"+email);

         //이메일을 입력 안 했을 경우 focus
        if (email.length() == 0) {
            Toast.makeText(LoginActivity.this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
            Email.requestFocus();
            return;
        }
        //password 입력 안 했을 경우 focus
        if (password.length() == 0) {
            Toast.makeText(LoginActivity.this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
            Password.requestFocus();
            return;
        }

        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("register.json");
        animationView.playAnimation();
        animationView.loop(true);
        animationView.resumeAnimation();


        Call<User> call = apiInterface.performUserLogin(email, password,token);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if (response.isSuccessful()){

                    if (response.body().getResponse().equals("ok")){
                        //내 이름으로 자동로그인 해줘

                        //이메일로 로그인 했다는 것, 자동로그인, 사용자 이메일을 저장한다.
                        prefConfig.writeLoginStatus(true);
                        prefConfig.writelogmethod("email");
                        prefConfig.displayToast(response.body().getName()+"님 환영합니다^");
                        prefConfig.writeEmail(response.body().getEmail());

                        //받은 토큰을 쉐어드에 저장함
                        prefConfig.storeToken(token);

                        //메인으로 이동할래
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else if (response.body().getResponse().equals("login failed")){
                        prefConfig.displayToast("이메일 또는 비밀번호를 잘못 입력하셨습니다.");
                    }

                }else{
                    prefConfig.displayToast("네트워크와 통신에 이상이 있습니다.");
                }
                animationView.pauseAnimation();
                animationView.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                prefConfig.displayToast("에러입니다. "+t);
                Log.d(TAG, "onFailure: "+t);
            }
        });
    }
}
