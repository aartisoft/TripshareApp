package com.example.tripshare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.LoginRegister.User;
import com.example.tripshare.imageFilter.FilterActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEdit extends AppCompatActivity {
    private static final String TAG = "Profile";
    private Button buttonedit;
    private CircleImageView circleImageView;
    public static  ApiInterface apiInterface;
    private String image, name,email, editimage,status, changedname;
    public static PrefConfig prefConfig;
    private EditText editname;
    private Uri photoUri;
    private Bitmap bitmap;
    public static final int FilterEdit = 1100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        //이미지 수정을 안했을 경우
        status = "notedit";

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("프로필 수정");

        buttonedit =findViewById(R.id.profile_button);
        circleImageView = findViewById(R.id.profile_img);
        editname = findViewById(R.id.profile_Edit_name);

        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        //shared사용해서 로그인한 유저 이메일 가져올려고
        prefConfig = new PrefConfig(this);
        email =prefConfig.readEmail();
        Log.d(TAG, "onCreate: email"+email);

        //프로필 이미지와 이름을 가져온다.
        image = getIntent().getStringExtra("image");
        name = getIntent().getStringExtra("name");
        Log.d(TAG, "onCreate: image"+image);

        //이미지와 텍스트 이미지와 텍스트뷰에 붙이기
        Glide.with(this).load(image).into(circleImageView);
        editname.setText(name);

        //이미지 변경할 때
        circleImageView.setOnClickListener(v -> onSelectImageClick());

        //수정 눌렀을 때
        buttonedit.setOnClickListener(v -> {
            changedname = editname.getText().toString();
            if (!changedname.equals("")){
                editprofile();
            }

        });

    }

    private void editprofile() {

        Log.d(TAG, "editprofile: status"+status);
        if (status.equals("edit")){
            editimage = imageToString();
        }else{
            editimage = "i wanna be programmer";
        }
        Log.d(TAG, "editprofile: editimage"+editimage);
        Log.d(TAG, "uploadImage: Image"+image);

        //서버에 데이터를 전송해서한다.
        Call<User> call = apiInterface.Editmyimage(email, changedname, editimage, status);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if (response.isSuccessful()){
                    User user = response.body();
                    Log.d(TAG, "onResponse:edit "+user.getResponse());
                    if (user.getResponse().equals("edit")){
                        prefConfig.writeimgurl(user.getImage());
                    }
                    prefConfig.writename(user.getName());
                   Intent intent = new Intent(ProfileEdit.this, MainActivity.class);
                    //메인으로 이동
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
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
                status = "edit";
                photoUri = result.getUri();
                Intent intent = new Intent(ProfileEdit.this, FilterActivity.class);
                intent.putExtra("uri",photoUri.toString());
                startActivityForResult(intent, FilterEdit);
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
                Toast.makeText(this, "프로필 사진을 수정했습니다. ", Toast.LENGTH_LONG).show();*/

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "이미지 수정을 취소했습니다." + result.getError(), Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == FilterEdit && resultCode == RESULT_OK){
            String path =  data.getStringExtra("path");
            Log.d(TAG, "onActivityResult:path "+ path);
            Log.d(TAG, "onActivityResult:uri "+Uri.parse(path));
            try {
                //사진을 bitmap으로 전환
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //고른 프로필 사진이 보이게 한다.
            circleImageView.setImageBitmap(bitmap);
            //Toast.makeText(this, "프로필 사진을 변경했습니다. ", Toast.LENGTH_LONG).show();
        }
    }

    //compress  and decoding image
    private String imageToString(){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //bitmap을 jpeg로 압축한다.
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        //byte를 인코딩해서 문자열로 만들어
        //bitmap을 문자열로 만듬
        Log.d(TAG, "imageToString: imgByte"+imgByte);
        Log.d(TAG, "imageToString: "+ Base64.encodeToString(imgByte, Base64.DEFAULT));
        return Base64.encodeToString(imgByte, Base64.DEFAULT);

    }



}
