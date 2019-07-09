package com.example.tripshare.TripTalk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tripshare.Adapter.FriendsAdapter;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.UserList;
import com.example.tripshare.LiveStream.StreamingActivity;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.LoginRegister.User;
import com.example.tripshare.MainActivity;
import com.example.tripshare.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    public static PrefConfig prefConfig;
    public static ApiInterface apiInterface;

    private String imgurl, name, email;
    private static final String TAG = "ChatActivity";
    private CircleImageView mycirimg;
    private TextView mynametx, totalfriendstx;
    private RecyclerView friendrecycler;
    private ArrayList<User> friendList;
    private FriendsAdapter friendsAdapter;
    private Context mtx;
    private LinearLayout myinlinear, bottomlinear;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        bottomlinear = findViewById(R.id.bottom_linear_chat);
        myinlinear = findViewById(R.id.myinfo_chat);
        mtx = getApplicationContext();
        //bottom navi view
        bottomNavigationView = findViewById(R.id.bottom_chat);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {

            switch (menuItem.getItemId()) {
                case R.id.action_friend:
                    break;
                case R.id.action_chatroom:
                    Intent chatintent = new Intent(ChatActivity.this, ChatroomActivity.class);
                    chatintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(chatintent);
                    break;
                case R.id.action_video:
                    Intent videointent = new Intent(ChatActivity.this,  StreamingActivity.class);
                    videointent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(videointent);
                    break;
            }
            return true;
        });

        //액션바 설정(제목,홈버튼,친구추가버튼)
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("친구");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //뷰 설정하기
        mycirimg = findViewById(R.id.cirimg_chat);
        mynametx = findViewById(R.id.myname_chat);
        totalfriendstx = findViewById(R.id.frinum_tx_chat);
        friendrecycler = findViewById(R.id.friendlist_recy_chat);
        //쉐어드에서 사용자 이메일,이름,이미지 가져오기
        prefConfig = new PrefConfig(this);
        imgurl = prefConfig.readimgurl();
        name = prefConfig.getName();
        email = prefConfig.readEmail();
        Log.d(TAG, "onCreate:name email url " + name + "\n" + imgurl + "\n" + email + "\n");

        //내 이름이랑 프사 보여주기
        mynametx.setText(name);
        Glide.with(this).load(imgurl).into(mycirimg);

        //친구목록 가져오기
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getfriendlist();
    }

    private void getfriendlist() {
        Call<UserList> call = apiInterface.myfriendlist(email);
        call.enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(Call<UserList> call, Response<UserList> response) {
                if (response.isSuccessful()) {
                    UserList userList = response.body();
                    if (userList.getResponse().equals("exist")) {
                        //등록된 친구가 1명이상 있는 경우
                        //총 인원 수
                        String totaltx = "친구 총 " + userList.getTotal() + "명";
                        totalfriendstx.setText(totaltx);
                        //인원리스트 받아 리사이클러뷰에 넣어줌
                        showfriendlist(userList.getmyfriendlist());
                    }


                } else {
                    Log.d(TAG, "onResponse: error");
                }
            }

            @Override
            public void onFailure(Call<UserList> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    private void showfriendlist(ArrayList<User> userLists) {
        friendList = new ArrayList<>();
        friendList = userLists;

        friendsAdapter = new FriendsAdapter(friendList, mtx);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this);
        friendrecycler.setLayoutManager(layoutManager);
        friendrecycler.setAdapter(friendsAdapter);
    }

    @Override //앱바 만드는 곳
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.triptalk, menu);

        MenuItem searchitem = menu.findItem(R.id.search_chat);
        SearchView searchView = (SearchView) searchitem.getActionView();

        //x버튼
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        //검색 버튼을 눌러서 시작이 되면 내 정보를 안보여 준다.
        //검색이 종료되면 내 정보를 다시 보여준다.
        searchitem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
           @Override
           public boolean onMenuItemActionExpand(MenuItem item) {
               myinlinear.setVisibility(View.GONE);
               bottomlinear.setVisibility(View.GONE);
               bottomNavigationView.setVisibility(View.GONE);
               return true;
           }

           @Override
           public boolean onMenuItemActionCollapse(MenuItem item) {
               myinlinear.setVisibility(View.VISIBLE);
               bottomlinear.setVisibility(View.VISIBLE);
               bottomNavigationView.setVisibility(View.VISIBLE);
               return true;
           }
       });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //엔터나 확인 버튼을 누르면 시작된다.
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            //검색하는 텍스트가 바뀔 때 마다 실행된다.
            @Override
            public boolean onQueryTextChange(String s) {
                friendsAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override   //앱바에서 지도, 뒤로가기 클릭할 때
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.plusfriend:
                Intent plusfriend = new Intent(ChatActivity.this, PlusfriendActivity.class);
                startActivity(plusfriend);
                Log.d(TAG, "onOptionsItemSelected: 친구 추가가 클릭됨");
                return true;
            case android.R.id.home:
                //메인으로
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
