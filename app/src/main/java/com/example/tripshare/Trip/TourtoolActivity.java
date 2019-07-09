package com.example.tripshare.Trip;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tripshare.Adapter.TourtoolAdapter;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.Example;
import com.example.tripshare.Data.TripData;
import com.example.tripshare.Data.Weather;
import com.example.tripshare.Fcm.Constants;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.LoginRegister.User;
import com.example.tripshare.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class TourtoolActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TourtoolActivity";
    private int tnum, term;
    private String tstart, tend, placeid, placename, countrycode;
    private double latitude, longitude;
    private TextView date, hotel, food, hotplace, textturm, tournametx, tourtimetx, localnametx, localtimetx, translatetx;
    private LinearLayout timelinearlayout, weatherlinearlayout, translatelilayout;
    private EditText translateetx;
    private Spinner translatespinner;
    private ImageView dateimg, hotelimg, foodimg, hotplaceimg, weatherdesimg;
    private Button friendplus, weatherbt, localtimebt, translateshowbt, resulttranslatebt;
    public static ApiInterface apiInterface, weatherapi, translateapi;
    public static PrefConfig prefConfig;
    public static String mydborgplace;
    Retrofit weatherretrofit, translateretrofit;
    ArrayList<Weather> weatherArrayList;
    private RecyclerView recyclerView;
    private TourtoolAdapter tourtoolAdapter;
    long starttodaydifferenc;
    String connectedtoday;
    String format = "json";
    String latlng;
    String key = "72623d06473e488c84d121418192506";
    int beforenum;
    String translatekey ="AIzaSyBdGjUa3OSXHOInYWbLdBCDnS4pHjtGme8";
    HashMap<String, String> firstyear;
    HashMap<String, String> secondyear;
    HashMap<String, String> thirdyear;
    String newstart;
    String endday;
    Date Startdate;
    Date enddate;
    Date today;
    String stasubday, endsubday;
    String source,inputtranslate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourtool);

        firstyear = new HashMap<>();
        secondyear = new HashMap<>();
        thirdyear = new HashMap<>();

        //날씨 리사이클러뷰
        recyclerView = findViewById(R.id.weather_recycler_Tourtool);
        //친구와 일정짜기 버튼
        friendplus = findViewById(R.id.friend_itinerary_TourTool);
        //일정관리와 도시에 대한 검색을 위한 버튼
        hotelimg = findViewById(R.id.hotel_tourtool_image);
        hotplaceimg = findViewById(R.id.hotplace_tourtool_image);
        hotel = findViewById(R.id.hotel_tourtool_text);
        hotplace = findViewById(R.id.hotplace_tourtool_text);
        date = findViewById(R.id.date_tourtool_text);
        foodimg = findViewById(R.id.food_tourtool_image);
        dateimg = findViewById(R.id.plan_tourtool_image);
        food = findViewById(R.id.food_tourtool_text);
        textturm = findViewById(R.id.term_tourtool_text);
        hotplace.setOnClickListener(this);
        hotplaceimg.setOnClickListener(this);
        hotelimg.setOnClickListener(this);
        hotel.setOnClickListener(this);
        dateimg.setOnClickListener(this);
        date.setOnClickListener(this);
        food.setOnClickListener(this);
        foodimg.setOnClickListener(this);
        friendplus.setOnClickListener(this);

        //날씨,현지 시간버튼
        localtimebt = findViewById(R.id.time_bt_tourtool);
        weatherbt = findViewById(R.id.weather_bt_TourTool);
        localtimebt.setOnClickListener(this);
        weatherbt.setOnClickListener(this);
        //날씨는 처음엔 클릭 안되게
        weatherbt.setEnabled(false);
        weatherbt.setBackgroundResource(android.R.color.darker_gray);
        //시간 텍스트, 레이아웃
        localtimetx = findViewById(R.id.localtime_tx_tourtool);
        localnametx = findViewById(R.id.localname_tx_tourtool);
        tournametx = findViewById(R.id.tourname_tx_tourtool);
        tourtimetx = findViewById(R.id.tourtime_tx_tourtool);
        timelinearlayout = findViewById(R.id.linear_inframe_tourtool);
        //날씨 버튼 클릭했을 때 리사이클러뷰 보여주기 위해 리사이클러뷰를 가진 리니어레이아웃
        //느낌표를 클릭하면 날씨에 대한 설명이 나옴
        weatherlinearlayout = findViewById(R.id.weather_linear_Tourtool);
        weatherdesimg = findViewById(R.id.weatherdesc_img_tourtool);
        weatherdesimg.setOnClickListener(this);

        //번역 레이아웃, 버튼 2개, 결과 보여줄 텍스트, 결과로 보여줄 언어를 고를 스피너까지
        resulttranslatebt = findViewById(R.id.translate_bt_tourtool); translateshowbt = findViewById(R.id.translatelayout_bt_tour);
        translatelilayout = findViewById(R.id.translate_inframe_tourtool); translateetx = findViewById(R.id.translate_edit_tourtool);
        translatespinner = findViewById(R.id.language_tx_tourtool); translatetx = findViewById(R.id.translated_tx_tourtool);

        translateshowbt.setOnClickListener(this);

        //메인화면에서 선택된 여행에 대한 정보를 받아옴
        placename = getIntent().getStringExtra("placename");
        term = getIntent().getIntExtra("term", -1);
        tnum = getIntent().getIntExtra("tnum", -1);
        tstart = getIntent().getStringExtra("tstart");
        tend = getIntent().getStringExtra("tend");
        placeid = getIntent().getStringExtra("placeid");
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        countrycode = getIntent().getStringExtra("countrycode");
        Log.d(TAG, "onCreate: lati" + latitude);
        //친구랑 일정짜기 기능을 위한 이메일 확인을 위해서
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        //누가, 누구를 초대했는지, 토큰에 대한정보를 가져오기 위해서
        prefConfig = new PrefConfig(this);

        Log.d(TAG, "onCreate: countrycode : " + countrycode);
        //액션바 사용할꺼 기능 : 여행 도시 보여주기, 뒤로가기, 맵 액션
        Log.d(TAG, "onCreate: 길이 " + countrycode.length());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(placename + " 여행");
        actionBar.setDisplayHomeAsUpEnabled(true);
        //여행 기간 보여주기
        String setturm = tstart + " ~ " + tend + "(" + term + "일)";
        textturm.setText(setturm);
        Log.d(TAG, "onCreate: " + tstart + "\n" + tend + "\n" + tnum + "\n" + term + "\n" + placeid + "\n" + placename + "\n");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Log.d(TAG, "onCreate: noti ");
            NotificationChannel mChannel = new NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(Constants.CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);

            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(mChannel);
        }

        //날씨 정보 요청
        weatherretrofit = new Retrofit.Builder()
                .baseUrl("http://api.worldweatheronline.com/premium/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherapi = weatherretrofit.create(ApiInterface.class);


        //World weather online에 날씨 정보를 요청한다.
        //api가 60일 무료라 더 사용하려면 돈 내야 한다.
        //받는 데이터는 json
        translateretrofit = new Retrofit.Builder()
                .baseUrl("https://translation.googleapis.com/language/translate/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        translateapi = translateretrofit.create(ApiInterface.class);
        String target= "ko";

        //언어 종류
        String[] languagelist = {"영어","스페인어","프랑스어","일본어","중국어","독일어",};
        //언어별 필요 값들
        HashMap<String, String> langmap = new HashMap<>();
        langmap.put("영어", "en");
        langmap.put("스페인어", "es");
        langmap.put("프랑스어", "fr");
        langmap.put("일본어", "ja");
        langmap.put("중국어", "zh");
        langmap.put("독일어", "de");

       //기본 해석은 영어다.
        source = "en";
        //언어들이 카테고리가 된다.
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(),
                R.layout.spintx,
                languagelist);
        translatespinner.setAdapter(arrayAdapter);
        //클릭했을 경우 언어 식별자를 가져온다.
        translatespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: "+languagelist[position]+"\n"+langmap.get(languagelist[position]));
                source = langmap.get(languagelist[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //번역버튼을 클릭했을 경우
        resulttranslatebt.setOnClickListener(v -> {
           //사용자가 입력한 값을 가져온다.
            inputtranslate = translateetx.getText().toString();
            //번역해달라고 구글에게 요청한다.
            //필요한 값은 사용자가 입력한 것
            Call<Example> call = translateapi.translate(inputtranslate,source,target,translatekey);
            call.enqueue(new Callback<Example>() {
                @Override
                public void onResponse(Call<Example> call, Response<Example> response) {
                    if (response.isSuccessful()){
                        Example example = response.body();
                        Log.d(TAG, "onResponse:text "+ example.getData().getTranslations().get(0).getTranslatedText());
                        String resulttx = example.getData().getTranslations().get(0).getTranslatedText();
                        translatetx.setText(resulttx);
                    }else {
                        Log.d(TAG, "onResponse: error");
                        Toast.makeText(TourtoolActivity.this, "결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Example> call, Throwable t) {
                    Log.d(TAG, "onFailure: "+t);
                    Toast.makeText(TourtoolActivity.this, "결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });


        });



        latlng = String.valueOf(latitude) + ',' + String.valueOf(longitude);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(calendar.YEAR);
        int month = calendar.get(calendar.MONTH) + 1;
        int day = calendar.get(calendar.DATE);
        connectedtoday = year + "-" + month + "-" + day;

        newstart = tstart.replace(".", "-");
        Log.d(TAG, "onCreate:newstart " + newstart);
        char firmon = tstart.charAt(5);

        endday = tend.replace(".", "-");

        //출발일 처리
        if (newstart.length() == 10) {
            //올바른 date 표현식 월,일이 다 표현됨
            stasubday = newstart;
        } else if (newstart.length() == 9) {
            if (firmon == 1) {
                //월에 1이 있다면 일이 한 자리 인데 0이 없는 것이다.
                //일에 0을 넣어 줄 예정
                stasubday = newstart.substring(0, 8) + "0" + newstart.substring(newstart.length() - 1);
                Log.d(TAG, "onCreate:sub 일에0 :" + stasubday);
            } else {
                //월이 한자리 인데 0인 것 월에 0을 넣어 줄 것이다.
                stasubday = newstart.substring(0, 5) + "0" + newstart.substring(5);
                Log.d(TAG, "onCreate:sub 월에 " + stasubday);
            }
        } else {
            //둘 다 한자리 인데 0이 없는 것
            // 둘 다 넣어줘
            stasubday = newstart.substring(0, 5) + "0" + newstart.substring(5, 7) + "0" + newstart.substring(newstart.length() - 1);
            Log.d(TAG, "onCreate:sub 둘다 " + stasubday);
        }

        //종료일 처리
        if (endday.length() == 10) {
            //올바른 date 표현식 월,일이 다 표현됨
            endsubday = endday;
        } else if (endday.length() == 9) {
            if (firmon == 1) {
                //월에 1이 있다면 일이 한 자리 인데 0이 없는 것이다.
                //일에 0을 넣어 줄 예정
                endsubday = endday.substring(0, 8) + "0" + endday.substring(endday.length() - 1);
                Log.d(TAG, "onCreate:sub 일에0 :" + endsubday);
            } else {
                //월이 한자리 인데 0인 것 월에 0을 넣어 줄 것이다.
                endsubday = endday.substring(0, 5) + "0" + endday.substring(5);
                Log.d(TAG, "onCreate:sub 월에 " + endsubday);
            }
        } else {
            //둘 다 한자리 인데 0이 없는 것
            // 둘 다 넣어줘
            endsubday = endday.substring(0, 5) + "0" + endday.substring(5, 7) + "0" + endday.substring(endday.length() - 1);
            Log.d(TAG, "onCreate:sub 둘다 " + endsubday);
        }


        @SuppressLint("SimpleDateFormat") SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            //비교할 날짜들을date 형으로 변환
            Startdate = format1.parse(stasubday);
            enddate = format1.parse(endsubday);
            today = format1.parse(connectedtoday);
            Log.d(TAG, "onCreate: today" + today);
            //날짜 차이 계산(1970년에서 몇 초가 흘렀는지)
            long caldate = Startdate.getTime() - today.getTime();
            Log.d(TAG, "onCreate: caldate" + caldate);

            //차이가지고 일 수가 나오게 함
            starttodaydifferenc = caldate / (24 * 60 * 60 * 1000);
            Log.d(TAG, "onCreate: caldate 나누기 :" + starttodaydifferenc);
            starttodaydifferenc = Math.abs(starttodaydifferenc);
            Log.d(TAG, "onCreate:결과 " + starttodaydifferenc);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //출발일과 오늘 날짜 차이가 10이 넘으면 이번 해에 예보를 요청
        if (starttodaydifferenc <= 10) {
            //문제는 출발일이 오늘 날짜보다 이후인지 이전인지 모르는 문제가 생김
            //이후라면 문제없이 미래에 대해서 예보를 요청하면 된다.
            //이전일 경우 생길 문제를 2가지 경우의 수로 해결함.

            int endcompareresult = endsubday.compareTo(connectedtoday);
            Log.d(TAG, "onCreate: compareend" + endcompareresult);

            Log.d(TAG, "onCreate: st,to" + stasubday + "\n" + connectedtoday);
            int startcompareresult = stasubday.compareTo(connectedtoday);
            Log.d(TAG, "onCreate:comparestart" + startcompareresult);

            if (startcompareresult <= -1) {
                //출발일이 오늘보다 이전인 경우 : 출발 날짜만 예보를 주는 문제가 생김
                //그런 경우 도착일이 오늘보다 이후인 경우, 이전인 경우에 따라 다르게 요청을 할 예정
                String term = String.valueOf(10);
                if (endcompareresult == 1 || endcompareresult == 0) {

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(today);
                    cal.add(Calendar.DATE, 1);
                    Log.d(TAG, "onCreate:현재날짜 + " + format1.format(cal.getTime()));
                    //도착일이 현재일 이후이거나 같다면 출발을 현재시간 , 10일의 예보를 받음
                    postrequest(term, format1.format(cal.getTime()));
                } else if (endcompareresult <= -1) {
                    //도착일 보다 현재일이 더 이후라면
                    //과거 예보요청
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(today);
                    cal.add(Calendar.DATE, 1);
                    Log.d(TAG, "onCreate:현재날짜 +2 " + format1.format(cal.getTime()));
                    postrequest(term, format1.format(cal.getTime()));
                }

            } else {
                //출발일이 오늘 날짜보다 이후인 경우 : 출발 날짜 기준으로 이번 해의 예보를 요청한다.
                if (term >= 14) {
                    //최대 14일
                    String sterm = String.valueOf(14);
                    postrequest(sterm, tstart);

                } else {
                    //그 이전은 이전 대로
                    String sterm = String.valueOf(term);
                    postrequest(sterm, tstart);

                }
            }

        } else {
            //과거 예보 요청
            beforeoneweather();


        }
    }

    private void postrequest(String term, String start) {
        Log.d(TAG, "postrequest:key "+key);
        Log.d(TAG, "postrequest:latlng "+latlng);
        Log.d(TAG, "postrequest:format "+format);
        Log.d(TAG, "postrequest:term "+term);
        Call<Example> call = weatherapi.postweather(key, latlng, format, term);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                if (response.isSuccessful()) {

                    Example example = response.body();

                    //날씨 정보가 있다면
                    if (example.getData().getWeather() != null && example.getData().getWeather().size() >= 1) {
                        weatherArrayList = new ArrayList<>();

                        Log.d(TAG, "onResponse:size " + example.getData().getWeather().size());
                        for (int i = 0; i < example.getData().getWeather().size(); i++) {

                            //데이터 받을 리스트 초기화 해줌
                            Weather weather = new Weather(example.getData().getWeather().get(i).getDate(),
                                    example.getData().getWeather().get(i).getMaxtempC(),
                                    example.getData().getWeather().get(i).getMintempC(),
                                    example.getData().getWeather().get(i).getHourly());
                            //데이터 넣어줌
                            weatherArrayList.add(weather);
                        }

                        //리사이클러뷰 만들어줌
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                        tourtoolAdapter = new TourtoolAdapter(getApplicationContext(), weatherArrayList);
                        recyclerView.setAdapter(tourtoolAdapter);
                    }


                } else {
                    Log.d(TAG, "onResponse: error");
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    private void beforerequest(String pastst, String pastend) {

        Call<Example> call = weatherapi.weather(key, latlng, format, pastst, pastend);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {

                if (response.isSuccessful()) {


                    Example example = response.body();
                    //마지막 3번째 요청
                    if (beforenum == 3) {

                        if (example.getData().getWeather() != null && example.getData().getWeather().size() >= 1) {
                            weatherArrayList = new ArrayList<>();
                            for (int i = 0; i < example.getData().getWeather().size(); i++) {
                                //여행 예정일의 과거에 있었던 날씨 중(최대 14일) 최고, 최저 온도와 총 강수량을 hashmap에 넣어 둔다.
                                thirdyear.put(i + "max", example.getData().getWeather().get(i).getMaxtempC());
                                thirdyear.put(i + "min", example.getData().getWeather().get(i).getMintempC());
                                //하루 총 강수량 뽑기 3시간 마다 강수량을 더함
                                double totalraindrop = 0;
                                for (int hours = 0; hours <= 7; hours++) {

                                    totalraindrop = totalraindrop + Double.valueOf(example.getData().getWeather().get(i).getHourly().get(hours).getRainperhour());
                                }
                                //소수점 2번째에서 반올림 함
                                @SuppressLint("DefaultLocale") String banorlinraindrop = String.format("%.1f", totalraindrop);
                                thirdyear.put(i + "raindrop", banorlinraindrop);

                            }

                            for (int i = 0; i < example.getData().getWeather().size(); i++) {
                                //3년의 통계를 통해 여행 요일들의  최고,최저기온, 하루 강수량 평균을 구한다.
                                String maxaverage = String.valueOf((Integer.valueOf(firstyear.get(i + "max")) + Integer.valueOf(secondyear.get(i + "max")) + Integer.valueOf(thirdyear.get(i + "max"))) / 3);
                                Log.d(TAG, "onResponse: maxaver " + maxaverage);
                                String minaverage = String.valueOf((Integer.valueOf(firstyear.get(i + "min")) + Integer.valueOf(secondyear.get(i + "min")) + Integer.valueOf(thirdyear.get(i + "min"))) / 3);
                                Log.d(TAG, "onResponse: minever " + minaverage);
                                String raindrop = String.valueOf((int) (Double.valueOf(firstyear.get(i + "raindrop")) + Double.valueOf(secondyear.get(i + "raindrop")) + Double.valueOf(thirdyear.get(i + "raindrop"))) / 3);
                                Log.d(TAG, "onResponse: maxaver " + raindrop);


                                //데이터 받을 리스트 초기화 해줌

                                Weather weather = new Weather(example.getData().getWeather().get(i).getDate(),
                                        maxaverage,
                                        minaverage,
                                        example.getData().getWeather().get(i).getHourly());
                                //데이터 넣어줌
                                weatherArrayList.add(weather);
                            }

                            //리사이클러뷰 만들어줌
                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                            tourtoolAdapter = new TourtoolAdapter(getApplicationContext(), weatherArrayList);
                            recyclerView.setAdapter(tourtoolAdapter);
                            Log.d(TAG, "onResponse:date size " + example.getData().getWeather().size());
                            Log.d(TAG, "onResponse:date " + example.getData().getWeather().get(0).getDate());
                            Log.d(TAG, "onResponse:max " + example.getData().getWeather().get(0).getMaxtempC());
                            Log.d(TAG, "onResponse:min " + example.getData().getWeather().get(0).getMintempC());
                            Log.d(TAG, "onResponse: " + example.getData().getWeather().get(0).getHourly().size());
                            Log.d(TAG, "onResponse: " + example.getData().getWeather().get(0).getHourly().get(0).getWeatherIconUrl().size());
                        }
                    } else if (beforenum == 1) {
                        if (example.getData().getWeather() != null && example.getData().getWeather().size() >= 1) {

                            for (int i = 0; i < example.getData().getWeather().size(); i++) {
                                //여행 예정일의 과거에 있었던 날씨 중(최대 14일) 최고, 최저 온도와 총 강수량을 hashmap에 넣어 둔다.
                                firstyear.put(i + "max", example.getData().getWeather().get(i).getMaxtempC());
                                firstyear.put(i + "min", example.getData().getWeather().get(i).getMintempC());
                                //하루 총 강수량 뽑기 3시간 마다 강수량을 더함
                                double totalraindrop = 0;
                                for (int hours = 0; hours <= 7; hours++) {

                                    totalraindrop = totalraindrop + Double.valueOf(example.getData().getWeather().get(i).getHourly().get(hours).getRainperhour());
                                }
                                //소수점 2번째에서 반올림 함
                                @SuppressLint("DefaultLocale") String banorlinraindrop = String.format("%.1f", totalraindrop);
                                firstyear.put(i + "raindrop", banorlinraindrop);
                            }
                        }
                        beforetwoweather();
                    } else if (beforenum == 2) {
                        if (example.getData().getWeather() != null && example.getData().getWeather().size() >= 1) {

                            for (int i = 0; i < example.getData().getWeather().size(); i++) {
                                //여행 예정일의 과거에 있었던 날씨 중(최대 14일) 최고, 최저 온도와 총 강수량을 hashmap에 넣어 둔다.
                                secondyear.put(i + "max", example.getData().getWeather().get(i).getMaxtempC());
                                secondyear.put(i + "min", example.getData().getWeather().get(i).getMintempC());
                                //하루 총 강수량 뽑기 3시간 마다 강수량을 더함
                                double totalraindrop = 0;
                                for (int hours = 0; hours <= 7; hours++) {

                                    totalraindrop = totalraindrop + Double.valueOf(example.getData().getWeather().get(i).getHourly().get(hours).getRainperhour());
                                }
                                //소수점 2번째에서 반올림 함
                                @SuppressLint("DefaultLocale") String banorlinraindrop = String.format("%.1f", totalraindrop);
                                secondyear.put(i + "raindrop", banorlinraindrop);
                            }
                        }
                        beforethreeweather();

                    }
                } else {
                    Log.d(TAG, "onResponse: error");
                }
            }


            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });

    }

    private void beforethreeweather() {
        beforenum = 3;

        String stdate = tstart.substring(4);
        String endate = tend.substring(4);
        String paststdate = "2018" + stdate;
        String pastendate = "2018" + endate;
        Log.d(TAG, "onCreate: " + pastendate + "\n" + paststdate);
        beforerequest(paststdate, pastendate);
    }

    private void beforeoneweather() {
        beforenum = 1;
        String stdate = tstart.substring(4);
        String endate = tend.substring(4);
        String paststdate = "2016" + stdate;
        String pastendate = "2016" + endate;
        beforerequest(paststdate, pastendate);
    }

    private void beforetwoweather() {
        beforenum = 2;
        String stdate = tstart.substring(4);
        String endate = tend.substring(4);
        String paststdate = "2017" + stdate;
        String pastendate = "2017" + endate;
        beforerequest(paststdate, pastendate);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume:countrycode "+countrycode);
        //해당 여행이 친구랑 같이 짜는 건지 혼자 짜는건지 알아보기
        Call<TripData> call = apiInterface.checkplus(tnum, placeid, tstart, tend, countrycode);
        call.enqueue(new Callback<TripData>() {
            @Override
            public void onResponse(Call<TripData> call, Response<TripData> response) {
                if (response.isSuccessful()) {
                    TripData tripData = response.body();
                    tnum = tripData.getTnum();
                    mydborgplace = tripData.getCountrycode();
                    Log.d(TAG, "onResponse:tnum" + tnum);
                    Log.d(TAG, "onResponse:check " + tripData.getCountrycode());
                    Log.d(TAG, "onResponse:response " + response.body().getResponse());
                    Log.d(TAG, "onResponse: " + tripData);
                    Log.d(TAG, "onResponse: " + response);
                } else {
                    Log.d(TAG, "onResponse: error");
                }
            }

            @Override
            public void onFailure(Call<TripData> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    @Override //앱바 만드는 곳
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.triptool, menu);
        return true;
    }

    @Override   //앱바에서 지도, 뒤로가기 클릭할 때
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map:
                Log.d(TAG, "onOptionsItemSelected: 맵이 클릭됨");
                return true;
            case android.R.id.home:
                //메인으로
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override //클릭 했을 때 이벤트들
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.date_tourtool_text:
            case R.id.plan_tourtool_image:
                Intent planintent = new Intent(TourtoolActivity.this, ItineraryActivity.class);
                planintent.putExtra("term", term);
                planintent.putExtra("placename", placename);
                planintent.putExtra("latitude", latitude);
                planintent.putExtra("longitude", longitude);
                planintent.putExtra("countrycode", countrycode);
                planintent.putExtra("tnum", tnum);
                startActivity(planintent);
                break;
            case R.id.hotel_tourtool_image:
            case R.id.hotel_tourtool_text:
                Intent hotel = new Intent(TourtoolActivity.this, SearchActivity.class);
                hotel.putExtra("tnum", tnum);
                hotel.putExtra("placename", placename);
                hotel.putExtra("tstart", tstart);
                hotel.putExtra("tend", tend);
                hotel.putExtra("latitude", latitude);
                hotel.putExtra("longitude", longitude);
                hotel.putExtra("term", term);
                hotel.putExtra("type", "호텔");
                startActivity(hotel);
                break;
            case R.id.food_tourtool_image:
            case R.id.food_tourtool_text:
                Intent foodintent = new Intent(TourtoolActivity.this, SearchActivity.class);
                foodintent.putExtra("tnum", tnum);
                foodintent.putExtra("placename", placename);
                foodintent.putExtra("tstart", tstart);
                foodintent.putExtra("tend", tend);
                foodintent.putExtra("latitude", latitude);
                foodintent.putExtra("longitude", longitude);
                foodintent.putExtra("term", term);
                foodintent.putExtra("type", "맛집");
                startActivity(foodintent);
                break;
            case R.id.hotplace_tourtool_image:
            case R.id.hotplace_tourtool_text:

                Intent hpintent = new Intent(TourtoolActivity.this, HotplaceActivity.class);
                hpintent.putExtra("tnum", tnum);
                hpintent.putExtra("term", term);
                hpintent.putExtra("tstart", tstart);
                hpintent.putExtra("tend", tend);
                hpintent.putExtra("countrycode", countrycode);
                hpintent.putExtra("method", mydborgplace);
                hpintent.putExtra("latitude", latitude);
                hpintent.putExtra("longitude", longitude);
                hpintent.putExtra("placename", placename);
                startActivity(hpintent);
                break;
            case R.id.friend_itinerary_TourTool:
//                //2개의 친구 추가 방법을 담을 리스트
//                List<String> listitem = new ArrayList();
//                listitem.add("이메일 초대");
//                listitem.add("카카오톡 초대");
//                //리스트를 배열로 바꾼다.
//                CharSequence[] items = listitem.toArray(new String [listitem.size()]);
//                //친구 추가 방법을 선택할 다이어로그
                AlertDialog.Builder builder = new AlertDialog.Builder(TourtoolActivity.this);
                builder.setTitle("친구와 일정 짜기");
//                builder.setItems(items, (dialog, which) -> {
//
//                    String selectedText = items[which].toString();
//                    //선택한 방법
//                        if (selectedText.equals("이메일 초대")){
//                        //선택한 방법이 이메일일 경우 이메일을 입력하라는 다이어로그 띄어줌
//
                EditText editText = new EditText(TourtoolActivity.this);
                AlertDialog.Builder builder1 = new AlertDialog.Builder(TourtoolActivity.this);
                builder1.setView(editText);
                builder1.setMessage("초대할 친구의 이메일을 입력해주세요");
                builder1.setTitle("친구 초대");
                builder1.setPositiveButton("확인", (dialog1, which1) -> {

                    String email = editText.getText().toString().trim();
                    String fromemail = prefConfig.readEmail();
                    String name = prefConfig.getName();
                    Log.d(TAG, "onClick: " + email + "\n" + fromemail + "\n" + name + "\n" + tnum + "\n" + placename);
                    Log.d(TAG, "onClick: " + placeid + "\n" + tstart + "\n" + tend);
                    //이메일이 등록된 이메일인지 체크
                    Call<User> call = apiInterface.friendnoti(email, fromemail, name, tnum, placename);
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.isSuccessful()) {
                                User user = response.body();
                                Log.d(TAG, "onResponse: " + user.getEmail());
                                Log.d(TAG, "onResponse: " + user.getResponse());

                                if (user.getResponse().equals("no email")) {
                                    Toast.makeText(TourtoolActivity.this, "해당 이메일을 가진 유저가 없습니다.", Toast.LENGTH_LONG).show();
                                } else if (user.getResponse().equals("already plused")) {
                                    Toast.makeText(TourtoolActivity.this, "해당 유저는 이미 초대 되었습니다.", Toast.LENGTH_SHORT).show();
                                } else if (user.getResponse().equals("email is")) {
                                    Toast.makeText(TourtoolActivity.this, "해당 유저를 초대했습니다.", Toast.LENGTH_LONG).show();
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

                }).//취소인 경우
                        setNegativeButton("취소", (dialog12, which12) -> {
                    Toast.makeText(this, "취소", Toast.LENGTH_SHORT).show();
                });
//                //선택한 방법이 카카오 톡 초대일 경우
//                setNeutralButton("카카오 톡 초대", (dialog, which) -> {
//                    Toast.makeText(this, "카카오 톡 초대", Toast.LENGTH_SHORT).show();
//                });
                builder1.show();
                break;
            case R.id.time_bt_tourtool:
                //현지 시간 요청

                Call<Example> call = weatherapi.tourcitytime(key, latlng, format);
                call.enqueue(new Callback<Example>() {
                    @Override
                    public void onResponse(Call<Example> call, Response<Example> response) {
                        if (response.isSuccessful()) {
                            Example example = response.body();
                            if (example.getData().getTimezone() != null && example.getData().getTimezone().size() >= 1) {
                                //timezone의 데이터가 1개 이상이라면
                                Log.d(TAG, "onResponse:time 1개 이상");
                                Log.d(TAG, "onResponse:time " + example.getData().getTimezone().get(0).getLocaltime());
                                //측정 지역을 본다.
                                Log.d(TAG, "onResponse:timezone " + example.getData().getTimezone().get(0).getZone());
                                String stand = example.getData().getTimezone().get(0).getZone();
                                //연월일과 시간을 나눈다
                                String[] tourarray = example.getData().getTimezone().get(0).getLocaltime().split(" ");
                                String tourymd = tourarray[0];
                                String tourtime = tourarray[1];
                                Log.d(TAG, "onResponse:tourtimetx " + tourymd + "\n" + tourtime);
                                //사용자에게 보여줄 시간 측정 도시 이름과 날짜
                                String tourcityname = stand + " - " + tourymd;
                                Log.d(TAG, "onResponse:tourtimetx " + tourcityname);

                                //사용자에게 보여줄 시간 tourtime

                                //현재 시간 얻기
                                long mnow = System.currentTimeMillis();
                                Date mdate = new Date(mnow);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
                                String ymdhm = simpleDateFormat.format(mdate);
                                Log.d(TAG, "onResponse:date " + mdate);
                                Log.d(TAG, "onResponse:date " + ymdhm);
                                //날짜와 현재 시간을 나눈다.
                                String[] localtimearray = String.valueOf(ymdhm).split(" ");
                                //날짜는 배열의 첫 번째 시간은 두 번째 인덱스에 위치한다.
                                String localymd = localtimearray[0];
                                String localtime = localtimearray[1];
                                String localkind = localtimearray[2];
                                Log.d(TAG, "onResponse:지금 " + localkind);
                                Log.d(TAG, "onResponse:지금 현지 시간 " + localymd + "\n" + localtime);
                                String localname = "서울 : " + localymd;
                                //리사이클러뷰 안보이게 ==invisible 처리
//                              //날씨 클릭 버튼 활성화, 배경색 변경
                                // weather click able하게, 배경 파란색 복귀
                                // 시간 click disable하게, 배경 회색으로
                                // 시간 가지고 있는 linear visible 처리, 안의 뷰들에 텍스트 넣어줌
                                weatherlinearlayout.setVisibility(View.INVISIBLE);
                                weatherbt.setEnabled(true);
                                weatherbt.setBackgroundResource(R.color.colorPrimary);

                                translatelilayout.setVisibility(View.INVISIBLE);
                                translateshowbt.setEnabled(true);
                                translateshowbt.setBackgroundResource(R.color.colorPrimary);

                                localtimebt.setEnabled(false);
                                localtimebt.setBackgroundResource(android.R.color.darker_gray);
                                timelinearlayout.setVisibility(View.VISIBLE);

                                tournametx.setText(tourcityname);
                                tourtimetx.setText(tourtime);
                                localnametx.setText(localname);
                                localtimetx.setText(localtime);

                            }

                        } else {
                            Log.d(TAG, "onResponse: error");
                            Toast.makeText(TourtoolActivity.this, "현지 시간을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Example> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t);
                        Toast.makeText(TourtoolActivity.this, "현지 시간을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();

                    }
                });
                break;
            case R.id.weather_bt_TourTool:
                //리사이클러뷰 보이게 하고, 시간 리니얼 레이아웃 안보이게한다.
                //날씨 버튼 클릭 비활성화 하고 시간 버튼 활성화 한다.
                //버튼 색깔도 변경해준다.
                weatherlinearlayout.setVisibility(View.VISIBLE);
                weatherbt.setEnabled(false);
                weatherbt.setBackgroundResource(android.R.color.darker_gray);

                timelinearlayout.setVisibility(View.INVISIBLE);
                localtimebt.setEnabled(true);
                localtimebt.setBackgroundResource(R.color.colorPrimary);

                translatelilayout.setVisibility(View.INVISIBLE);
                translateshowbt.setEnabled(true);
                translateshowbt.setBackgroundResource(R.color.colorPrimary);

                break;
            case R.id.weatherdesc_img_tourtool:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(TourtoolActivity.this);
                builder2.setTitle("날씨?");
                builder2.setMessage("--오늘일 기준으로 10일 이후의 날씨는 World Weather Online에서 제공하는 최근 3년간의 평균 데이터를 기반으로 노출됩니다."+"\n"+
                        "--10일 이내의 날씨는 같은 곳에서 제공되는 일기 예보로 확인할 수 있습니다.");
                builder2.setPositiveButton("확인", (dialog, which) -> {
                  dialog.dismiss();
                });
                builder2.show();
                break;
            case R.id.translatelayout_bt_tour:
                weatherlinearlayout.setVisibility(View.INVISIBLE);
                weatherbt.setEnabled(true);
                weatherbt.setBackgroundResource(R.color.colorPrimary);

                timelinearlayout.setVisibility(View.INVISIBLE);
                localtimebt.setEnabled(true);
                localtimebt.setBackgroundResource(R.color.colorPrimary);

                translatelilayout.setVisibility(View.VISIBLE);
                translateshowbt.setEnabled(false);
                translateshowbt.setBackgroundResource(android.R.color.darker_gray);
                break;
        }
    }


}

