package com.example.tripshare.TripTalk;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.LoginRegister.User;
import com.example.tripshare.MainActivity;
import com.example.tripshare.R;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlusfriendActivity extends AppCompatActivity {
    public static PrefConfig prefConfig;
    public static ApiInterface apiInterface;
    private String useremail, friemail, friurl, friname, searchresult, searchemail;
    private TextView noresulttx, nametx;
    private Button plusbt, searchbt;
    private CircleImageView imgcir;
    private LinearLayout existlinear;
    private EditText emailedit;
    private Context mctx;

    private static final String TAG = "PlusfriendActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plusfriend);

        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        //액션바 만들기
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("이메일로 친구 추가");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mctx = getApplicationContext();
        //뷰들 사용하기
        noresulttx = findViewById(R.id.noresult_tx_plusfriend);
        nametx = findViewById(R.id.name_tx_plusfriend);
        imgcir = findViewById(R.id.img_circle_plusfriend);
        plusbt = findViewById(R.id.plus_bt_plusfrend);
        existlinear = findViewById(R.id.exist_linear_plusfriend);
        emailedit = findViewById(R.id.email_et_plusfriend);
        searchbt = findViewById(R.id.search_bt_plusfriend);

        //사용자 email
        prefConfig = new PrefConfig(this);
        useremail = prefConfig.readEmail();
        Log.d(TAG, "onCreate: useremail " + useremail);

        //이메일 검색했을 때
        searchbt.setOnClickListener(v -> {
            friemail = emailedit.getText().toString();
            if (!friemail.equals("")) {
                Call<User> call = apiInterface.Searchfriend(friemail);
                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            User user = response.body();
                            searchresult = user.getResponse();
                            if (!searchresult.equals("empty")) {
                                //이메일이 있을 경우 사용자의 이름과 url을 가져옴
                                //레이아웃 보여주고 이미지와 이름은 넣어줌.
                                existlinear.setVisibility(View.VISIBLE);
                                noresulttx.setVisibility(View.INVISIBLE);

                                searchemail = user.getEmail();
                                friurl = user.getImage();
                                friname = user.getName();
                                Log.d(TAG, "onResponse: email" + searchemail + " url :" + friurl);
                                Glide.with(mctx).load(friurl).into(imgcir);
                                nametx.setText(friname);
                            } else {
                                //이메일이 없을 경우 없다고 사용자에게 알려주기
                                existlinear.setVisibility(View.INVISIBLE);
                                noresulttx.setVisibility(View.VISIBLE);
                            }
                            Log.d(TAG, "onResponse:result ");

                        } else {
                            Log.d(TAG, "onResponse: error");
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t);
                    }
                });
            }
        });

        plusbt.setOnClickListener(v -> {

            if (!useremail.equals(friemail)) {
                //사용자 이메일이랑 친구 이메일이랑 다를경우에만 추가한다.
                Call<User> call = apiInterface.plusfriend(useremail, searchemail);
                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            User user = response.body();
                            String plusresult = user.getResponse();
                            Log.d(TAG, "onResponse: result" + plusresult);
                            if (plusresult.equals("success")) {
                                //성공했을 때
                                Intent gochat = new Intent(PlusfriendActivity.this, ChatActivity.class);
                                gochat.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(gochat);
                            } else if (plusresult.equals("already")) {
                                Toast.makeText(PlusfriendActivity.this, "이미 친구에 등록되있습니다.", Toast.LENGTH_SHORT).show();

                            } else {
                                //실패했을 때
                                Toast.makeText(PlusfriendActivity.this, "친구 추가에 실패했습니다.", Toast.LENGTH_SHORT).show();

                            }

                        } else {
                            Log.d(TAG, "onResponse: error");
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t);
                    }
                });
            }

        });


    }

    @Override   //앱바에서 지도, 뒤로가기 클릭할 때
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //메인으로
                Intent intent = new Intent(PlusfriendActivity.this, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
