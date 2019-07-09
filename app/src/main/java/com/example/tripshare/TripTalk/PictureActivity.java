package com.example.tripshare.TripTalk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tripshare.Adapter.PictureAdapter;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.Message;
import com.example.tripshare.Data.Messagelist;
import com.example.tripshare.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PictureActivity extends AppCompatActivity {
    ViewPager viewPager;
    PictureAdapter pictureAdapter;
    ArrayList<Message> messageslist;
    int clickposition;
    private static final String TAG = "PictureActivity";
    ImageView backimg;
    TextView name, date;
    ConstraintLayout constraintLayout;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String rnum;
    ArrayList<Message> imagelist;
    public static ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        /*messageslist =(ArrayList<Message>) getIntent().getSerializableExtra("messagelist");
        Log.d(TAG, "onCreate: message size : "+messageslist.size());*/
        rnum = getIntent().getStringExtra("rnum");
        clickposition = getIntent().getIntExtra("clickposition",0);
        Log.d(TAG, "onCreate: position "+clickposition);
        Log.d(TAG, "onCreate: rnum"+rnum);
        constraintLayout = findViewById(R.id.conlayout_picture);
        name = findViewById(R.id.name_tx_picture);
        date= findViewById(R.id.date_tx_picture);
        backimg = findViewById(R.id.back_img_picture);
        viewPager = findViewById(R.id.viewpager_picture);

        getMessagelist();

        constraintLayout.setOnClickListener(v -> {

            if (name.getVisibility() == View.VISIBLE){
                name.setVisibility(View.INVISIBLE);
                date.setVisibility(View.INVISIBLE);
                backimg.setVisibility(View.INVISIBLE);
            }else if (name.getVisibility() == View.INVISIBLE){
                name.setVisibility(View.VISIBLE);
                date.setVisibility(View.VISIBLE);
                backimg.setVisibility(View.VISIBLE);
            }
        });

        backimg.setOnClickListener(v -> {
            finish();
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.d(TAG, "onPageScrollStateChanged: "+i);
                //0은 다른 것으로 변경하든 안 했든 결과가 난다면 0이 호출됨
                if (i == 0){
                    int posi = viewPager.getCurrentItem();
                    Log.d(TAG, "onPageScrollStateChanged: posi"+posi);
                    name.setText(imagelist.get(posi).getSendername());
                    date.setText(imagelist.get(posi).getYmd());
                }
            }
        });
    }

    private void getMessagelist(){

        //이미지 서버에서 가져오기
        Call<Messagelist> call = apiInterface.imagemessagelist(rnum);
        call.enqueue(new Callback<Messagelist>() {
            @Override
            public void onResponse(Call<Messagelist> call, Response<Messagelist> response) {
                if (response.isSuccessful()){

                    imagelist = new ArrayList<>();

                    imagelist = response.body().getMessageslist();

                    setviewpager();
                }else {
                    Log.d(TAG, "onResponse:error ");
                }
            }

            @Override
            public void onFailure(Call<Messagelist> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t);
            }
        });

        //이미지 기기에서 가져온는 것
        /* sharedPreferences = getSharedPreferences(rnum, MODE_PRIVATE);
        String message =  sharedPreferences.getString("Message","");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Message>>(){}.getType();
        ArrayList<Message> messageArrayList = gson.fromJson(message, type);
        //이미지 메세지만 따로 만든다.
        Log.d(TAG, "getMessagelist: "+messageArrayList.size() );
        imagelist = new ArrayList<>();
        for (int i = 0; i<messageArrayList.size() ; i++){
            if (messageArrayList.get(i).getType().equals("image")){
                imagelist.add(messageArrayList.get(i));
            }
        }*/

    }

    private void setviewpager() {
        Log.d(TAG, "getMessagelist:size "+imagelist.size());
        Log.d(TAG, "getMessagelist:posi "+clickposition);
       /* if (imagelist.get(clickposition).getBitmaptoString()!= null){
            Log.d(TAG, "getMessagelist:posiimg "+imagelist.get(clickposition).getBitmaptoString());
        }else {
            Log.d(TAG, "getMessagelist:posiurl "+imagelist.get(clickposition).getMessage());
        }*/
        //리사이 클러뷰에 넣어준다.
        pictureAdapter = new PictureAdapter(this,imagelist);
        viewPager.setAdapter(pictureAdapter);
        viewPager.setCurrentItem(clickposition);

        //현재 사진 올린 사람 이름,날짜 보이게하기
        name.setText(imagelist.get(clickposition).getSendername());
        date.setText(imagelist.get(clickposition).getYmd());
    }
}
