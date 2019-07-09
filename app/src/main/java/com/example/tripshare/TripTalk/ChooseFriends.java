package com.example.tripshare.TripTalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.tripshare.Adapter.ChooseAdapter;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.Room;
import com.example.tripshare.Data.UserList;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.LoginRegister.User;
import com.example.tripshare.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooseFriends extends AppCompatActivity {

    public static PrefConfig prefConfig;
    private String myemail;
    ApiInterface apiInterface;
    private static final String TAG = "ChooseFriends";
    public static ArrayList<User> userArrayList, selectedfriendlist;
    private RecyclerView friendrecycler;
    private ChooseAdapter chooseAdapter;
    String othersemail,othersname;
    private String receiver, rnum;
    public static String choosereason, choosetotal,chooseemail;
    private String ymd,hm;
    private String chooseedname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_friends);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("대화할 친구 선택");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (getIntent().getStringExtra("email") ==null){
            //개설인 경우
            choosereason = "개설";

        }else {
            //추가인 경우
            choosereason = "추가";
            chooseemail = getIntent().getStringExtra("email");
            choosetotal = getIntent().getStringExtra("total");
            receiver = getIntent().getStringExtra("name");
            rnum = getIntent().getStringExtra("rnum");
            Log.d(TAG, "onCreate:plus "+choosetotal+"\n"+chooseemail+"\n"+receiver+"\n"+rnum+"\n");
        }


        friendrecycler = findViewById(R.id.recy_choosefriends);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ChooseFriends.this);
        friendrecycler.setLayoutManager(layoutManager);

        //체크박스를 눌렀을 경우
        friendrecycler.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    View view = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                    int position = recyclerView.getChildAdapterPosition(view);
                    LinearLayout linearLayout = (LinearLayout) view;

                    //리니어 자식뷰 가져오기
                    int count = linearLayout.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View element = linearLayout.getChildAt(i);

                        //linear 가져오기
                        if (element instanceof LinearLayout) {
                            LinearLayout childlinear = (LinearLayout) element;
                            Log.d(TAG, "onInterceptTouchEvent: 자식 리니어레이아웃 가져옴");
                            //check box 가져오기
                            View chelement = childlinear.getChildAt(0);
                            if (chelement instanceof CheckBox) {
                                Log.d(TAG, "onInterceptTouchEvent: 자식 리니어의 자식 체크박스 가져옴");
                                CheckBox checkBox = (CheckBox) chelement;
                                checkBox.setOnClickListener(v -> {
                                    //체크박스 클릭했을 때 클릭 되었다고 해주기
                                    if (((CheckBox)v).isChecked()){

                                        userArrayList.get(position).setCheckedornot(true);
                                        Log.d(TAG, "onClick: 체크박스 클릭됨");
                                    }else {
                                        userArrayList.get(position).setCheckedornot(false);
                                        Log.d(TAG, "onClick: 체크박스 클릭삭제");
                                    }
                                });
                            }
                        }
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });

        prefConfig = new PrefConfig(this);
        myemail = prefConfig.readEmail();
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        Call<UserList> call = apiInterface.myfriendlist(myemail);
        call.enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(Call<UserList> call, Response<UserList> response) {
                if (response.isSuccessful()) {
                    UserList userList = response.body();
                    myfriendslist(userList.getmyfriendlist());

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
    //내 친구리스트 가져오기
    private void myfriendslist(ArrayList<User> userList) {
        if (userList != null) {
            //전체 친구들이 체크 모두 안된 상태로 만들기
            userArrayList = userList;
            //전체 친구들이 체크 모두 안된 상태로 만들기
            for (int i =0 ; i < userArrayList.size() ; i++){
                userArrayList.get(i).setCheckedornot(false);
            }
            chooseAdapter = new ChooseAdapter(userArrayList, getApplicationContext());
            friendrecycler.setAdapter(chooseAdapter);
        }

    }

    private void getymdhm() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy년 M월 d일 E-a K시 m분");
        Date date = new Date();
        String today = df.format(date);
        String[] todayarray = today.split("-");
        ymd = todayarray[0];
        hm = todayarray[1];
        Log.d(TAG, "onCreate: " + ymd + "시간" + hm);
    }

    @Override //앱바 만드는 곳
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choosefriends, menu);

        MenuItem searchitem = menu.findItem(R.id.search_choose);
        SearchView searchView = (SearchView) searchitem.getActionView();

        //x버튼
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //엔터나 확인 버튼을 누르면 시작된다.
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            //검색하는 텍스트가 바뀔 때 마다 실행된다.
            @Override
            public boolean onQueryTextChange(String s) {
                chooseAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override   //앱바에서 지도, 뒤로가기 클릭할 때
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done_choose:
                //체크박스 클릭된 유저들만 따로 모와 채팅방 만들기
                selectedfriendlist = new ArrayList<>();
                for (int i = 0; i<userArrayList.size() ; i++){
                    if (userArrayList.get(i).isCheckedornot()){
                        selectedfriendlist.add(userArrayList.get(i));
                    }
                }
                String total = String.valueOf(selectedfriendlist.size());
                //체크박스에 클릭된 유저 이메일과 이름을 한 문자열로 만들기
                othersemail = "";
                Log.d(TAG, "onOptionsItemSelected: 총 선택된 인원: "+total);
                
                if (!total.equals("0")){
                    //한 명 이상 선택됬을 경우
                    for (int i = 0 ; i<selectedfriendlist.size() ; i++){
                        othersemail = othersemail+selectedfriendlist.get(i).getEmail()+",";
                    }
                    othersemail = othersemail.substring(0,othersemail.length()-1);
                    Log.d(TAG, "onOptionsItemSelected:다른 사람들 이메일 "+othersemail);
                    //이름을 한 문자열로 만들기
                    chooseedname = "";
                    for (int i = 0 ; i<selectedfriendlist.size() ; i++){
                        chooseedname = chooseedname+selectedfriendlist.get(i).getName()+",";
                    }

                    chooseedname = chooseedname.substring(0,chooseedname.length()-1);
                    Log.d(TAG, "onOptionsItemSelected:선택된 사람들 이름 "+chooseedname);

                    if (choosereason.equals("개설")){
                        //개설의 경우
                        Call<Room> call = apiInterface.savegroupchat(myemail, othersemail, total);
                        call.enqueue(new Callback<Room>() {
                            @Override
                            public void onResponse(Call<Room> call, Response<Room> response) {
                                if (response.isSuccessful()){
                                    if (response.body().getResponse().equals("success")){


                                        Log.d(TAG, "onResponse:save success");
                                        String rnumz = response.body().getRnum();
                                        String yourname = response.body().getYourname();
                                        Log.d(TAG, "onResponse: yourname"+yourname);
                                        Log.d(TAG, "onResponse: rnum"+rnumz);
//                                        if (total.equals("1")){
//                                          int arnum = Integer.valueOf(rnumz)+1;
//                                          rnumz = String.valueOf(arnum);
//                                        }

                                        Intent intent = new Intent(ChooseFriends.this, MessageActivity.class);
                                        intent.putExtra("email", othersemail);
                                        intent.putExtra("total",total);
                                        intent.putExtra("name",chooseedname);
                                        intent.putExtra("rnum",rnumz);
                                        startActivity(intent);
                                        finish();
                                    }
                                }else {
                                    Log.d(TAG, "onResponse: error");
                                }
                            }

                            @Override
                            public void onFailure(Call<Room> call, Throwable t) {
                                Log.d(TAG, "onFailure: "+t);
                            }
                        });
                    }else {

                        //추가의 경우 선택된 이메일과 이름을 합치고 총 인원을 더한다.
                        int intotal = Integer.valueOf(choosetotal)+selectedfriendlist.size();
                        //추가한 날짜와 시간을 얻어온다.
                        getymdhm();

                        othersemail = chooseemail+","+othersemail;
                        othersname = receiver+","+chooseedname;
                        String sttotal = String.valueOf(intotal);
                        Log.d(TAG, "onOptionsItemSelected:추가 이후 전체 인원 "+sttotal);
                        Log.d(TAG, "onOptionsItemSelected:추가 이후 이메일과 이름 "+othersemail+"\n"+othersname);
                        Log.d(TAG, "onOptionsItemSelected: "+ymd+"\n"+hm+"\n"+myemail+"\n"+rnum);


                        Call<Room> call = apiInterface.saveplusedemail(myemail, othersemail, rnum, sttotal,othersname,ymd,hm,chooseedname);
                        call.enqueue(new Callback<Room>() {
                            @Override
                            public void onResponse(Call<Room> call, Response<Room> response) {
                                if (response.isSuccessful()){
                                    Log.d(TAG, "onResponse: "+response.body().getResponse());
                                    Intent intent = new Intent(ChooseFriends.this, MessageActivity.class);
                                    intent.putExtra("add","add");
                                    intent.putExtra("email", othersemail);
                                    intent.putExtra("total",sttotal);
                                    intent.putExtra("name",chooseedname);
                                    intent.putExtra("rnum",rnum);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    Log.d(TAG, "onResponse: error");
                                }
                            }
                            @Override
                            public void onFailure(Call<Room> call, Throwable t) {
                                Log.d(TAG, "onFailure: "+t);
                            }
                        });
                    }
                }else {
                    Toast.makeText(this, "친구를 선택해주세요", Toast.LENGTH_SHORT).show();
                }
                return true;
            case android.R.id.home:
                if (choosereason.equals("개설")){
                    Intent intent = new Intent(ChooseFriends.this, ChatroomActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }else {
                    finish();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
