package com.example.tripshare;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tripshare.Adapter.TriplistAdapter;
import com.example.tripshare.Ar.ArActivity;
import com.example.tripshare.Data.TripData;
import com.example.tripshare.Data.TripList;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.LoginRegister.User;
import com.example.tripshare.Token.WalletPassword;
import com.example.tripshare.TripTalk.ChatActivity;
import com.example.tripshare.TripTalk.TalkService;
import com.example.tripshare.WhereWhen.LocationAutocompleteActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    public static PrefConfig prefConfig;
    private TextView textView,tx;
    private Button BnLogOut;
    private String email, method, imgstring,name;
    private CircleImageView circleImageView;
    private LinearLayout linearLayout;
    //drawer, toggle
    private DrawerLayout mdrawerLayout;
    private ActionBarDrawerToggle mToggle;
    //서버에게 프로필이미지 받기
    public static  ApiInterface apiInterface;
    private View header;
    private  NavigationView navigationView;
    private String way;
    private String placename, placeid,startdate,enddate,token;
    private int term,tnum;
    private static ArrayList<TripData> tripDataArrayList =new ArrayList<>();
    private RecyclerView tRecyclerView;
    private TriplistAdapter tAdapter;
    private TextView nontriptx;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nontriptx = findViewById(R.id.nontrip_tx_main);
        mdrawerLayout = findViewById(R.id.drawer);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        makenotichannel();


       /* 카카오 로그인 위한 프로젝트 키 받는 것
        String key = getHashKey(getApplicationContext());
        Log.d(TAG, "onCreate:key "+key);*/

        if (!isServiceRunning()){
            //서비스가 실행되있지 않는 경우에만 서비스 실행함
            Log.d(TAG, "onCreate: service");
            Intent serintent = new Intent(MainActivity.this, TalkService.class);
            startService(serintent);
        }else {
            Log.d(TAG, "onCreate: 서비스 이미 실행 중이여서 다시 실행 안 함");
        }

        //여는 것과 닫는 버튼을 갖은 토글 버튼을 만든다.
        mToggle = new ActionBarDrawerToggle(this, mdrawerLayout, R.string.open, R.string.close);
        //토글 버튼을 누르면 드러우가 열리게 한다.
        mdrawerLayout.addDrawerListener(mToggle);
        //메뉴의 각각의 아이템 클릭을 하기위해
        navigationView = findViewById(R.id.navi);
        //토글 버튼을 누르면 바뀔 수 있게
        // 열린 상태인지 닫힌 상태인지 동기화 한다.
        mToggle.syncState();
        //액션바에 있는 홈버튼 누르면 홈으로 가게한다.
        setupDrawerContent(navigationView);
        //header에 있는 뷰를 사용하기 위해 인플레이트 한다.
        header = getLayoutInflater().inflate(R.layout.header,null,false);
        //네비게이션에 헤더를 더한다.

        //header의 view를 통해 프로필 수정을 할 예정이다.
        tx=header.findViewById(R.id.header_textview);
        circleImageView = header.findViewById(R.id.header_circleimgview);
        linearLayout = header.findViewById(R.id.header_LinearLayout);

        //액션바가 나오게 한다.
        ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("가고 싶은 나라,도시");


        //쉐어드에 저장된 이메일이랑 로그인 방식 가져옴
        prefConfig = new PrefConfig(this);
        email = prefConfig.readEmail();
        method = prefConfig.readlogmethod();

        //노티를 클릭했을 경우 일정 짜기 초대받은 거다.
        //초대 받았을 경우 dialog가 떠사 수락을 했을 경우 여행 일정에 추가된다.
        String invite = getIntent().getStringExtra("invite");
        if (invite ==null){
            Log.d(TAG, "onCreate:invite  null");
        }else {

            //노티를 클릭하면 받는 데이터(누가, 어떤메세지를, 어떤 여행을 초대하는거야)
            String message = getIntent().getStringExtra("message");
            String fromemail = getIntent().getStringExtra("fromemail");
            int tnum = Integer.valueOf(invite);
            Log.d(TAG, "onCreate: tnum "+tnum);
            Log.d(TAG, "onCreate: email"+email);
            Log.d(TAG, "onCreate: fromemail"+fromemail);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("계획 수락");
            builder.setMessage(message);
            builder.setPositiveButton("수락", ((dialog, which) -> {

            Call<User> call = apiInterface.friendtrip(tnum, email,fromemail);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()){
                        User user = response.body();
                        Log.d(TAG, "onResponse:response friend "+user.getResponse());
                        givemytriplist(email);

                    }else {
                        Log.d(TAG, "onResponse: error");
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.d(TAG, "onFailure: "+t);
                }
            });

            })).setNegativeButton("거절",((dialog, which) -> {

            }));
            builder.show();
        }


        linearLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileEdit.class);
            intent.putExtra("image", imgstring);
            intent.putExtra("name", name);
            startActivity(intent);
        });

        //이메일을 서버로 보낼 인터페이스
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        navigationView.addHeaderView(header);
        //navigationView.removeHeaderView(header);
        Log.d(TAG, "onResume: "+email);
        //이메일을 서버로 보낼 요청


        Call<User> call = apiInterface.Givememyimage(email);
        //요청 결과
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()){
                    User user =  response.body();
                    Log.d(TAG, "user image: "+user.getImage());
                    Log.d(TAG, "user name: "+user.getName());
                    Log.d(TAG, "user name: "+user.getResponse());


                    imgstring = user.getImage();
                    Log.d(TAG, "onResponse: img"+imgstring);
                    name = user.getName();
                    Log.d(TAG, "onResponse: name"+name);
                    //이름, 사진을 다른 액티비티에서 사용하기 위해 쉐어드에 저장한다.
                    prefConfig.writename(name);
                    prefConfig.writeimgurl(imgstring);
                    //이미지 수정하거나 앱을 킬때 가져온다.
                  Glide.with(header.getContext()).load(imgstring).into(circleImageView);

                    tx.setText(name);

                }else{
                    Log.d(TAG, "onResponse:  is not successful");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "onResponse:  is failled"+t);
            }
        });

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

    // 프로젝트의 해시키를 반환
   /* @Nullable
    public static String getHashKey(Context context) {
        final String TAG = "KeyHash";
        String keyHash = null;
        try {
            PackageInfo info =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = new String(Base64.encode(md.digest(), 0));
                Log.d(TAG, keyHash);
            }
        } catch (Exception e) {
            Log.e("name not found", e.toString());
        }
        if (keyHash != null) {
            return keyHash;
        } else {
            return null;
        }
    }*/
    private void makenotichannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Log.d(TAG, "onCreate: noti ");
            NotificationChannel mChannel = new NotificationChannel(TalkService.CHAT_CHANNEL_ID, TalkService.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(TalkService.CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);

            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(mChannel);
            Log.d(TAG, "makenotichannel: ");
        }
    }

    //서비스 실행 여부 확인
    private boolean isServiceRunning() {

        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)){
            if (TalkService.class.getName().equals(serviceInfo.service.getClassName()))
                return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: done");
        givemytriplist(email);

    }

    private void givemytriplist(String email) {
        Call<TripList> callfirst = apiInterface.Givememytrip(email);
        callfirst.enqueue(new Callback<TripList>() {
            @Override
            public void onResponse(Call<TripList> call, Response<TripList> response) {
                generateEmployeeList(response.body().getTripArrayList());
            }
            @Override
            public void onFailure(Call<TripList> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t);
                Log.d(TAG, "onFailure: ca"+call);
                Toast.makeText(MainActivity.this, "에러...다시 시도 해주세요!"+t, Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: done");
    }

    /*Method to generate List of employees using RecyclerView with custom adapter*/
    private void generateEmployeeList(ArrayList<TripData> empDataList) {
        Log.d(TAG, "generateEmployeeList:empdatalist.size() "+empDataList.size());

        if (empDataList.size() ==0){
        //데이터가 0개이면 텍스트뷰 보여주기
          nontriptx.setVisibility(View.VISIBLE);
          return;
        }
        nontriptx.setVisibility(View.INVISIBLE);
        tRecyclerView = (RecyclerView) findViewById(R.id.triplist_recyclerview);

        tAdapter = new  TriplistAdapter(getApplicationContext(), empDataList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);

        tRecyclerView.setLayoutManager(layoutManager);

        tRecyclerView.setAdapter(tAdapter);

    }

    @Override //토글 버튼에서 '메뉴' 또는 '나가기'를 선택했을 때 무었을 선택했는지 알려줌
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //drawer에서 아이템을 선택했을 때의 효과
    public void selectItemDrawer(MenuItem menuItem){
        //클릭할 수 있게 해준다.
        // menuItem.setChecked(true);
        //선택된 아이템의 이름을 가져옴
        switch (menuItem.getItemId()){

            case R.id.plus:
                Intent intent = new Intent(MainActivity.this, LocationAutocompleteActivity.class);
                startActivity(intent);
                break;

            case R.id.logout :
                logoutPerformed();
                Log.d(TAG, "selectItemDrawer: "+"로그아웃 선택됨");
                break;

            case R.id.chat :
                Intent chatintent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(chatintent);
                break;
            case R.id.ar :
                Intent arintent = new Intent(MainActivity.this, ArActivity.class);
                startActivity(arintent);
                break;
            case R.id.ethereum :
                Intent walletint = new Intent(MainActivity.this, WalletPassword.class);
                startActivity(walletint);
                break;
            case R.id.game :
                String project = "com.example.Cube";
                Intent cubeintent = getPackageManager().getLaunchIntentForPackage(project);
                cubeintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(cubeintent);
                break;
        }

        setTitle(menuItem.getTitle());
        //drawer를 닫는다.
        mdrawerLayout.closeDrawers();
    }
    //drawer에 클릭 리스너 달아줌 아이템마다 각기 다른 이벤트를 위해.
    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                selectItemDrawer(menuItem);
                return false;
            }
        });

    }


    private void logoutPerformed() {

        method = prefConfig.readlogmethod();
        Log.d(TAG, "logoutPerformed: "+method);

        switch (method){

            case "email" :
                prefConfig.writeLoginStatus(false);
                prefConfig.writeEmail("User");
                new Thread(() -> {
                    try {
                        //로그인 방법이 카카오든 이메일이든 기기가 가지고 있는 토큰을 제거함
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                        Intent intent = new Intent(MainActivity.this, com.example.tripshare.LoginRegister.SNSloginActivity.class);
                        startActivity(intent);
                        finish();
                        Log.d(TAG, "onClick: deleted");
                    } catch (IOException e) {
                        Log.d(TAG, "onClick:error "+e.toString());
                        e.printStackTrace();
                    }
                }).start();
                break;

            case "kakao" :

                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() { //카카오 이메일을 디비에서 삭제한다.
                        Call<User> call = apiInterface.performlogout(email);
                        call.enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                if (response.isSuccessful()){

                                    User user =  response.body();
                                    Log.d(TAG, "onResponse: "+user.getResponse());

                                    new Thread(() -> {
                                        try {
                                            //로그인 방법이 카카오든 이메일이든 기기가 가지고 있는 토큰을 제거함
                                            FirebaseInstanceId.getInstance().deleteInstanceId();
                                            Intent intent = new Intent(MainActivity.this, com.example.tripshare.LoginRegister.SNSloginActivity.class);
                                            startActivity(intent);
                                            finish();
                                            Log.d(TAG, "onClick: deleted");
                                        } catch (IOException e) {
                                            Log.d(TAG, "onClick:error "+e.toString());
                                            e.printStackTrace();
                                        }
                                    }).start();

                                }else{
                                    Toast.makeText(MainActivity.this, "이상", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<User> call, Throwable t) {
                                Log.d(TAG, "onFailure: "+t);
                            }
                        });
                    }
                });
                prefConfig.writeLoginStatus(false);
                prefConfig.writeEmail("User");
                break;
        }


    }


}

//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d(TAG, "onResume: ");
//
//        takedata();
//        loaddata();
//        generateEmployeeList(tripDataArrayList);
//    }
//    //저장된 데이터 가져오기
//    private void takedata() {
//
//        SharedPreferences sharedPreferences = getSharedPreferences(email, Context.MODE_PRIVATE);
//        Gson gson = new Gson();
//        Log.d(TAG, "gson 확인"+gson);
//        Log.d(TAG, "String json = sharedPreferences.getString(\"Data\",  null);실행");
//        String json = sharedPreferences.getString("script",  null);
//        Log.d(TAG, "json 확인"+json);
//        Log.d(TAG, " Type type = new TypeToken<ArrayList<VideoDic>>() {}.getType();실행");
//        //json을 Arraylist로 바꿔준다.
//        Type type = new TypeToken<ArrayList<TripData>>() {}.getType();
//        Log.d(TAG, "type 확인"+type);
//        Log.d(TAG, "mArrayList = gson.fromJson(json, type);실행");
//        tripDataArrayList = gson.fromJson(json, type);
//        Log.d(TAG, "mArrayList확인"+tripDataArrayList);
//
////        if (mArrayList ==null) {  //onCreateView에서 만들어 줄 것을 여기서 먼저 만들어준의미
////            Log.d(TAG, " mArrayList ==null");
////            mArrayList = new ArrayList<VideoDic>();
////        }
//
//    }
//
//    private void loaddata() {
//        Log.d(TAG, "loaddata: ");
//        //사용자가 추가나 수정을 했을 경우
//        if (getIntent().getStringExtra("way") != null){
//            way =getIntent().getStringExtra("way");
//            switch (way){
//
//                case "장소수정":
//
//
//                    break;
//                case "추가":
//                  //  tripDataArrayList = new ArrayList<>();
//                    Log.d(TAG, "loaddata: "+getIntent().getIntExtra("tnum",0)+"\n"+ getIntent().getStringExtra("placename")
//                            +"\n"+getIntent().getStringExtra("placeid")+"\n"+getIntent().getIntExtra("howlong",0)
//                            +"\n"+getIntent().getStringExtra("startdate")+"\n"+getIntent().getStringExtra("enddate")+"\n");
//                   tnum =getIntent().getIntExtra("tnum",0);
//                   placename= getIntent().getStringExtra("placename");
//                   placeid = getIntent().getStringExtra("placeid");
//                   term = getIntent().getIntExtra("howlong",0);
//                   startdate=  getIntent().getStringExtra("startdate");
//                   enddate = getIntent().getStringExtra("enddate");
//                    TripData tripData = new TripData(placename, startdate, enddate, term, placeid, tnum);
//
//                    tripDataArrayList.add(0, tripData);
//                    Log.d(TAG, "loaddata: 처음에 넣음"+tripData);
//                    generateEmployeeList(tripDataArrayList);
//
//
//                    break;
//                case "날짜수정":
//
//
//                    break;
//            }
//
//
//        }
//
//
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        saveData();
//    }
//
//    private void saveData() {
//
//        //sharedpreferences 객체를 만듬
//        SharedPreferences sharedPreferences = getSharedPreferences(email, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        //저장을 위해 edit 객체를 만듬
//        Log.d(TAG, "Gson gson = new Gson();실행");
//        //gson 객체를 만든다.
//        Gson gson = new Gson();
//        Log.d(TAG, "gson확인"+gson);
//        Log.d(TAG, " String json = gson.toJson(mArrayList);실행");
//        //mArrayList를 Json형식으로 가지고 있다.
//        String json = gson.toJson(tripDataArrayList);
//        Log.d(TAG, "json확인 "+json);
//        editor.putString("script", json);
//        Log.d(TAG, "  editor.putString(\"script\", json);종료");
//        editor.apply();
//
//    }

