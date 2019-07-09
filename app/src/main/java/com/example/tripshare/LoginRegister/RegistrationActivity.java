package com.example.tripshare.LoginRegister;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.MainActivity;
import com.example.tripshare.ProfileEdit;
import com.example.tripshare.R;
import com.example.tripshare.imageFilter.FilterActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegistrationActivity extends AppCompatActivity {
    private EditText Email, Name, Password, RePassword;
    private Button BnRegister;
    private static final String TAG = "register";
    public static ApiInterface apiInterface;
    public static PrefConfig prefConfig;
    private String email, password, repassword, name;
    private CircleImageView circleImageView;
    private Uri photoUri;
    private Bitmap bitmap;
    private LottieAnimationView animationView;
    private String image;
    private ImageView back;
    private boolean choose = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        //쉐어드 생성을 위해 반복 코드 줄이고
        prefConfig = new PrefConfig(this);

        //?? api 객체를 만든다.라고 생각하자.
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        Email = findViewById(R.id.reg_email_edit);
        Name = findViewById(R.id.reg_name_edit);
        Password = findViewById(R.id.reg_pass_edit);
        RePassword = findViewById(R.id.reg_repass_edit);
        BnRegister = findViewById(R.id.reg_bn);
        circleImageView = findViewById(R.id.reg_CirImgview);
        back = findViewById(R.id.reg_back_imgview);
        //회원가입시 로딩 보여주는 애니메이션
        animationView = (LottieAnimationView) findViewById(R.id.reg_lottie);


        back.setOnClickListener(v -> finish());

        //이미지 변경
        circleImageView.setOnClickListener(v -> onSelectImageClick());
        //회원가입
        BnRegister.setOnClickListener(v -> {

            email = Email.getText().toString();
            name = Name.getText().toString();
            password = Password.getText().toString();
            repassword = RePassword.getText().toString();


            checkEmailPassword();
        });
    }

    //이메일, 패스워드 체크
    private void checkEmailPassword() {


        // 이메일 입력 확인 아무것도 안 쓰이면 포커즈함
        if (email.length() == 0) {
            Toast.makeText(RegistrationActivity.this, "Email을 입력하세요", Toast.LENGTH_SHORT).show();
            Email.requestFocus();
            return;
        }
        //이메일 형식 체크
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(RegistrationActivity.this, "Email형식에 맞추세요", Toast.LENGTH_SHORT).show();
            Email.requestFocus();
            return;
        }
        // 이름 입력 확인 아무것도 안 쓰이면 포커즈함
        if (name.length() == 0) {
            Toast.makeText(RegistrationActivity.this, "이름를 입력하세요", Toast.LENGTH_SHORT).show();
            Name.requestFocus();
            return;
        }
        // 비밀번호 입력 확인 아무것도 안 쓰이면 포커즈함
        if (password.length() == 0) {
            Toast.makeText(RegistrationActivity.this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
            Password.requestFocus();
            return;
        }
        // 비밀번호 확인 입력 확인 아무것도 안 쓰이면 포커즈함
        if (repassword.length() == 0) {
            Toast.makeText(RegistrationActivity.this, "비밀번호 확인을 입력하세요", Toast.LENGTH_SHORT).show();
            RePassword.requestFocus();
            return;
        }

        // 비밀번호 일치 확인 아무것도 안 쓰이면 포커즈하고 텍스트를 빈칸처리
        if (!password.equals(repassword)) {
            Toast.makeText(RegistrationActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            RePassword.setText("");
            Password.setText("");
            Password.requestFocus();
            return;
        }

        performRegistration();
    }


    public void onSelectImageClick() {
        //이미지를 선택하고 다른 액티비티로 가서 크롭할 것
        CropImage.activity(null)
                .setGuidelines(CropImageView.Guidelines.ON).start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //crop 이미지 인 경우
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //크롭된 사진 uri
                photoUri = result.getUri();
                //프로필 사진 추가했으면 해당 사진을 서버에서 저장할 수 있게 해줌
                choose = true;

                Intent intent = new Intent(RegistrationActivity.this, FilterActivity.class);
                intent.putExtra("uri", photoUri.toString());
                startActivityForResult(intent, ProfileEdit.FilterEdit);
               /* try {
                //사진을 bitmap으로 전환
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),photoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //고른 프로필 사진이 보이게 한다.
                circleImageView.setImageBitmap(bitmap);
                Log.d(TAG, "result.uri: "+photoUri);
                Log.d(TAG, "result: "+result);
                Log.d(TAG, "photoUri to string: "+photoUri.toString());
                Toast.makeText(this, "프로필 사진을 추가했습니다. ", Toast.LENGTH_LONG).show();*/
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "이미지 추가를 취소했습니다." + result.getError(), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == ProfileEdit.FilterEdit && resultCode ==RESULT_OK) {

            assert data != null;
            String path = data.getStringExtra("path");
            //사진을 bitmap으로 전환
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(path));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //고른 프로필 사진이 보이게 한다.
            circleImageView.setImageBitmap(bitmap);
        }
    }

    //compress  and decoding image
    private String imageToString() {
        choose = true;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //bitmap을 jpeg로 압축한다.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        //byte를 인코딩해서 문자열로 만들어
        //bitmap을 문자열로 만듬
        Log.d(TAG, "imageToString: imgByte" + imgByte);
        Log.d(TAG, "imageToString: " + Base64.encodeToString(imgByte, Base64.DEFAULT));
        return Base64.encodeToString(imgByte, Base64.DEFAULT);

    }


    public void performRegistration() {
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("register.json");
        animationView.playAnimation();
        animationView.loop(true);
        animationView.resumeAnimation();

        if (choose) {
            //사진 선택시
            image = imageToString();
            Log.d(TAG, "uploadImage: Image" + image);

            //서버에 데이터를 전송해서한다.
            Call<User> call = apiInterface.performRegistration(email, name, password, image);
            //응답을 받아온다.
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {

                    if (response.isSuccessful()) {
                        User user = response.body();
                        Log.d(TAG, "onResponse: ");
                        if (user.getResponse().equals("ok")) {

                            prefConfig.displayToast("Registration success!");
                            Log.d(TAG, "user image " + user.getImage());
                            Log.d(TAG, "user image " + user.getEmail());
                            prefConfig.writelogmethod("email");
                            prefConfig.writeLoginStatus(true);
                            prefConfig.displayToast(response.body().getName() + "님 환영합니다^");
                            prefConfig.writeEmail(response.body().getEmail());
                            //메인으로 이동할래
                            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                            startActivity(intent);
                            animationView.pauseAnimation();
                            animationView.setVisibility(View.INVISIBLE);
                            finish();

                        } else if (user.getResponse().equals("exist")) {
                            prefConfig.displayToast("해당 이메일 유저가 이미 있습니다.");
                            Log.d(TAG, "user image " + user.getImage());
                            Log.d(TAG, "user image " + user.getEmail());
                        } else if (user.getResponse().equals("error")) {
                            prefConfig.displayToast("Something went wrong!@!");
                            Log.d(TAG, "user image " + user.getImage());
                            Log.d(TAG, "user image " + user.getEmail());
                            Log.d(TAG, "user image " + user.getName());
                        }

                        animationView.pauseAnimation();
                        animationView.setVisibility(View.INVISIBLE);


                    } else {
                        User user = response.body();
                        Log.d(TAG, "onResponse: " + user.getResponse());
                        Log.d(TAG, "user image " + user.getImage());
                        Log.d(TAG, "user image " + user.getEmail());
                    }
                    animationView.pauseAnimation();
                    animationView.setVisibility(View.INVISIBLE);

                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.d(TAG, "onFailure:에러 " + t + call);
                }
            });

        } else {
            //이미지 추가하지 않은 경우
            image = "http://bii755.vps.phps.kr/userimage/kidmili@naver.com.jpg";
            Log.d(TAG, "noImage: Image" + image);

            //서버에 데이터를 전송해서한다.
            Call<User> call = apiInterface.performDeImgregister(email, name, password, image);
            //응답을 받아온다.
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {

                    if (response.isSuccessful()) {
                        User user = response.body();
                        Log.d(TAG, "onResponse: ");
                        if (user.getResponse().equals("ok")) {

                            prefConfig.displayToast("Registration success!");
                            Log.d(TAG, "user image " + user.getImage());
                            Log.d(TAG, "user image " + user.getEmail());
                            prefConfig.writelogmethod("email");
                            prefConfig.writeLoginStatus(true);
                            prefConfig.displayToast(response.body().getName() + "님 환영합니다^");
                            prefConfig.writeEmail(response.body().getEmail());
                            //메인으로 이동할래
                            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                            startActivity(intent);
                            animationView.pauseAnimation();
                            animationView.setVisibility(View.INVISIBLE);
                            finish();

                        } else if (user.getResponse().equals("exist")) {
                            prefConfig.displayToast("해당 이메일 유저가 이미 있습니다.");
                            Log.d(TAG, "user image " + user.getImage());
                            Log.d(TAG, "user image " + user.getEmail());
                        } else if (user.getResponse().equals("error")) {
                            prefConfig.displayToast("Something went wrong!@!");
                            Log.d(TAG, "user image " + user.getImage());
                            Log.d(TAG, "user image " + user.getEmail());
                            Log.d(TAG, "user image " + user.getName());
                        }

                        animationView.pauseAnimation();
                        animationView.setVisibility(View.INVISIBLE);


                    } else {
                        User user = response.body();
                        Log.d(TAG, "onResponse: " + user.getResponse());
                        Log.d(TAG, "user image " + user.getImage());
                        Log.d(TAG, "user image " + user.getEmail());
                    }
                    animationView.pauseAnimation();
                    animationView.setVisibility(View.INVISIBLE);

                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.d(TAG, "onFailure:에러 " + t + call);
                }
            });

        }
    }
}
