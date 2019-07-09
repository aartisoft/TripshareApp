package com.example.tripshare.Trip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tripshare.Adapter.HotPlaceAdapter;
import com.example.tripshare.Adapter.SearchAdapter;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.Results;
import com.example.tripshare.Data.ResultsList;
import com.example.tripshare.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.tripshare.Trip.TourtoolActivity.mydborgplace;

public class HotplaceActivity extends AppCompatActivity {

    private String countrycode, method, placename,tstart,tend;
    private static final String TAG = "HotplaceActivity";
    private int term,tnum;
    private String pagetoken, url;
    private static final String REQUEST_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBdGjUa3OSXHOInYWbLdBCDnS4pHjtGme8";
    private LottieAnimationView lottieAnimationView;
    //데이터 담길 리스트
    private ArrayList<Results> hotpllist;
    int paging;
    private RecyclerView recyclerView;
    private Spinner cityspin, typespin;
    HotPlaceAdapter hotPlaceAdapter;
    String[] city;
    public static ApiInterface apiInterface;
    //for spinner
    private Context mctx;
    String cityselected;
    String typeselected;
    Button chbutton;
    TextView txhotplace;
    LinearLayout spinlinearLayout;
    boolean gorequest;
    int numrequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotplace);
        mctx = getApplicationContext();
        paging =0;
        //google place로 처음 페이징
        pagetoken = "first";
        lottieAnimationView = findViewById(R.id.lottie_hotplace);
        countrycode = getIntent().getStringExtra("countrycode");
        method = getIntent().getStringExtra("method");
        placename = getIntent().getStringExtra("placename");
        tstart = getIntent().getStringExtra("tstart");
        tend = getIntent().getStringExtra("tend");
        term = getIntent().getIntExtra("term",0);
        tnum = getIntent().getIntExtra("tnum",0);
        Log.d(TAG, "onCreate: " + countrycode + "\n" + method);

        //google에게 받은 관광지 데이터가 저장되는 arraylist
        Log.d(TAG, "onCreate:static " + mydborgplace);
        recyclerView = findViewById(R.id.recyclerView_hotplace);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(HotplaceActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        //다음 관광지 더 달라는 요청이다.
        //처음 요청은 바로 주니까 두 번째부터 생각한다.
        gorequest = true;
        numrequest = 2;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastvisibleitemposition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition() + 1;
                int itemTotalCount = recyclerView.getAdapter().getItemCount();
                Log.d(TAG, "onScrolled:last total " + lastvisibleitemposition + "\n" + itemTotalCount);

                //사용자가 recyclerview의 마지막 관광지를 보고 있다면
                if (lastvisibleitemposition == itemTotalCount) {
                    Log.d(TAG, "onScrolled:gorequest "+gorequest);
                    //사용자가 다음 관광지를 요청한 적이 없다면
                    if (gorequest) {
                        gorequest = false;
                        if (method.equals("mydb")){
                            nextplpaging(cityselected, typeselected, countrycode, numrequest);
                        }else {
                            if (numrequest != 5){
                                try {
                                    requestgplace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                    }

                }

            }
        });


        chbutton = findViewById(R.id.choose_bt_hotplace);
        txhotplace = findViewById(R.id.result_tx_hotplace);
        spinlinearLayout = findViewById(R.id.mydb_spinlinear_hotplace);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        if (method.equals("gplace")) {
            //구글에 관광지 요청
            //스피너 안보이게 한다.
            spinlinearLayout.setVisibility(View.GONE);

            //데이터 받을 리스트 초기화해서 대기시킨다.
            hotpllist = new ArrayList<>();
            try {
                requestgplace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else if (method.equals("mydb")) {
            //내 디비에 저장된 크롤링된 관광지를 가져올 경우
            cityspin = findViewById(R.id.mydb_cityspinner_hotplace);
            typespin = findViewById(R.id.mydb_typespinner2_hotplace);
            loading(true);
            Call<ResultsList> call = apiInterface.spotlist(countrycode);
            call.enqueue(new Callback<ResultsList>() {
                @Override
                public void onResponse(Call<ResultsList> call, Response<ResultsList> response) {
                    if (response.isSuccessful()) {

                        //카테고리 역할을 할 도시들을 배열로 보내서 문자열 배열로 받는다.
                        city = response.body().getResponse();
                        //처음 선택된 것은 전체이다.
                        cityselected = "전체";
                        typeselected = "전체";
                        for (int i = 0; i < city.length; i++) {
                            Log.d(TAG, "onResponse:선택할 도시 값 " + city[i]);
                        }
                        //장소 리스트를 서버에서 받아옴
                        spotlist(response.body().getResultsList());
                        makespinner();
                        loading(false);
                    } else {
                        loading(false);
                        Log.d(TAG, "onResponse: error");
                    }
                }

                @Override
                public void onFailure(Call<ResultsList> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t);
                    loading(false);
                }
            });

        }

        chbutton.setOnClickListener(v -> {
            //선택된 것으로 검색 한다.

            otherspot(cityselected, typeselected);
        });
    }

    private void requestgplace() throws UnsupportedEncodingException {
        loading(true);
        Log.d(TAG, "requestplace " + createurl());
        DownloadRawData downloadRawData = new DownloadRawData();
        //맛집 데이터를 주세요! 요청
        downloadRawData.execute(createurl());
    }

    private String createurl() throws UnsupportedEncodingException {

        String query = "tourist in " + placename;
        String urlOrigin = URLEncoder.encode(query, "utf-8");
        if (pagetoken.equals("first")) {
            //처음 정보를 가져올때
            url = REQUEST_URL + "query=" + urlOrigin + "&key=" + GOOGLE_API_KEY;
        } else {
            //두 번째, 세 번째 가져올 때
            Log.d(TAG, "createurl: pagetoken" + pagetoken);
            url = REQUEST_URL + "query=" + urlOrigin + "&pagetoken=" + pagetoken + "&key=" + GOOGLE_API_KEY;
        }
        Log.d(TAG, "createurl: " + url);
        return url;
    }


    //다음 관광지를를 요청하는 것
    private void nextplpaging(String cityselected, String typeselected, String countrycode, int numrequest) {
        loading(true);
        Call<ResultsList> call = apiInterface.plusspot(cityselected, typeselected, countrycode, numrequest);
        call.enqueue(new Callback<ResultsList>() {
            @Override
            public void onResponse(Call<ResultsList> call, Response<ResultsList> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse:next " + response.body().getResponse()[0]);
                    plusplace(response.body().getResultsList());

                } else {
                    Log.d(TAG, "onResponse:error ");
                }
            }

            @Override
            public void onFailure(Call<ResultsList> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });

    }

    private void plusplace(ArrayList<Results> resultsList) {


        if (resultsList.size() != 0) {
            Log.d(TAG, "plusplace:before add size " + resultsList.size());
            //기존 관광지에 추가된 데이터를 전부 뒤에 넣는다.
            int start = (numrequest - 1) * 15 - 1;
            int end = (numrequest) * 15 - 1;
            Log.d(TAG, "plusplace: st,en " + start + end);
            hotpllist.addAll(resultsList);
            hotPlaceAdapter = new HotPlaceAdapter(mctx, hotpllist);
            hotPlaceAdapter.notifyItemRangeInserted(start, end);
            recyclerView.setAdapter(hotPlaceAdapter);
            recyclerView.scrollToPosition(start);
            //다시 다음페이지를 요청할 수 있게 한다.
            Log.d(TAG, "plusplace:added size+num " + hotpllist.size());
            gorequest = true;
            numrequest = numrequest + 1;

        } else {
            //추가해줄 데이터가 없을 경우
            Toast.makeText(mctx, "더 이상 관광지가 없습니다.", Toast.LENGTH_SHORT).show();
        }
        //추가될 관광지가 있든  없든 로딩을 끈다.
        loading(false);

    }

    //카테고리를 만들기 위해 장소,테마 배열을 만듬
    //테마 카테고리는 3개로 정해져있으나 장소는 유동적이므로 서버에서 받은 장소 배열을 이용한다.
    private void makespinner() {


        //도시가 카테고리가 된다.
        ArrayAdapter arrayAdapter = new ArrayAdapter(mctx,
                R.layout.spintx,
                city);
        arrayAdapter.setDropDownViewResource(R.layout.spin_dropdown);
        cityspin.setAdapter(arrayAdapter);

        cityspin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + cityselected + typeselected);
                cityselected = city[position];
                Log.d(TAG, "onItemSelected: " + cityselected + typeselected);

//            Toast.makeText(HotplaceActivity.this, city[position] + "is selected " + cityspin.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //여행지 종류가 또 다른 카테고리
        String[] type = {"전체", "관광지", "레저/액티비티", "나이트라이프"};
        ArrayAdapter typearrayAdapter = new ArrayAdapter(mctx,
                R.layout.spintx,
                type);
        typearrayAdapter.setDropDownViewResource(R.layout.spin_dropdown);
        typespin.setAdapter(typearrayAdapter);

        typespin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + cityselected + typeselected);
                typeselected = typespin.getSelectedItem().toString();
                Log.d(TAG, "onItemSelected: " + cityselected + typeselected);
//                Toast.makeText(HotplaceActivity.this, type[position] + "is selected " + typespin.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void otherspot(String city, String kind) {
        loading(true);
        Log.d(TAG, "otherspot: city, kind, countrycode" + city + "\n" + kind + "\n" + countrycode);
        Call<ResultsList> call = apiInterface.selectedspot(city, kind, countrycode);
        call.enqueue(new Callback<ResultsList>() {
            @Override
            public void onResponse(Call<ResultsList> call, Response<ResultsList> response) {
                if (response.isSuccessful()) {
                    String[] re = response.body().getResponse();
                    Log.d(TAG, "onResponse: " + re[0]);
                    //장소 리스트를 서버에서 받아옴
                    changedspot(response.body().getResultsList());
                    loading(false);
                    //추가 정보 요청은 다시 2번째로 다시 초기화
                    numrequest = 2;
                } else {
                    loading(false);
                    Log.d(TAG, "onResponse: error");
                }
            }

            @Override
            public void onFailure(Call<ResultsList> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
                loading(false);
            }
        });

    }

    //사용자가 도시,종류 카테고리에서 선택했을 경우 리사이클러뷰에 다시 등록해줌
    private void changedspot(ArrayList<Results> resultsList) {
        if (resultsList.size() == 0) {
            txhotplace.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            return;
        }
        recyclerView.setVisibility(View.VISIBLE);
        txhotplace.setVisibility(View.INVISIBLE);


        hotpllist = resultsList;
        Log.d(TAG, "changedspot: " + hotpllist.get(0).getName() + "\n" + hotpllist.size());
        hotPlaceAdapter = new HotPlaceAdapter(mctx, hotpllist);
        hotPlaceAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(hotPlaceAdapter);

    }

    //처음에 리사이클러뷰에 보여줌
    private void spotlist(ArrayList<Results> resultsList) {
        if (resultsList.size() == 0) {
            txhotplace.setVisibility(View.VISIBLE);
            return;
        }
        txhotplace.setVisibility(View.INVISIBLE);
        hotpllist = new ArrayList<>();
        hotpllist = resultsList;
        hotPlaceAdapter = new HotPlaceAdapter(mctx, hotpllist);
        recyclerView.setAdapter(hotPlaceAdapter);
    }
    //데이터 받을 때 필요한 로딩
    private void loading(boolean load) {

        if (load) {
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.setAnimation("register.json");
            lottieAnimationView.playAnimation();
            lottieAnimationView.loop(true);
        } else {
            lottieAnimationView.pauseAnimation();
            lottieAnimationView.setVisibility(View.INVISIBLE);
        }

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
                Intent intent = new Intent(HotplaceActivity.this, MapsActivity.class);
                intent.putExtra("latitude",getIntent().getDoubleExtra("latitude",0));
                intent.putExtra("longitude",getIntent().getDoubleExtra("longitude",0));
                intent.putExtra("tnum",tnum);
                intent.putExtra("term",term);
                intent.putExtra("placename",placename);
                intent.putExtra("tstart",tstart);
                intent.putExtra("tend",tend);
                intent.putExtra("resultslist", hotpllist);
                startActivity(intent);

                return true;
            case android.R.id.home:
                //메인으로
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onsearchSuccess(ArrayList<Results> addedhotpllists) {

    //처음에 들어왔을 때
        if (numrequest ==2){
            hotPlaceAdapter = new HotPlaceAdapter(getApplicationContext(), addedhotpllists);
            recyclerView.setAdapter(hotPlaceAdapter);
            numrequest = numrequest+1;
        }else {
            hotPlaceAdapter = new HotPlaceAdapter(getApplicationContext(), addedhotpllists);
            hotPlaceAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(hotPlaceAdapter);
            numrequest = numrequest+1;
        }
        Log.d(TAG, "onsearchSuccess: num"+numrequest);
        //다시 요청할 수 있게
        gorequest =true;
        loading(false);
    }

    public void onsearchFail() {
//        다시 요청할 수 있게
        gorequest = true;
        loading(false);

    }

    @SuppressLint("StaticFieldLeak")
    class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //url을 받아온다.
            Log.d(TAG, "doInBackground: params : " + params.toString());
            Log.d(TAG, "doInBackground: params[0] : " + params[0]);
            String link = params[0];
            try {
                URL url = new URL(link);
                Log.d(TAG, "doInBackground: url /" + url);
                //데이터를 해당 url에 접속해 받는다.
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                //받은 데이터에서 한글등 아스키 코드가 아닌 것들이 안깨지게 Reader로 읽는다.
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    //json으로 된 데이터를 한 줄씩 데이터로 만들어
                    //문자열에 한줄 씩 추가한다.
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                //url이 잘못 전달 된경우
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        //전달받은 장소 없으면 끝냄
        if (data == null)
            return;

        Log.d(TAG, "parseJSon: Data " + data);

        //서버에게 받은 raw한 모든 내용을 대한 문자열을
        //json으로 만든다.
        JSONObject jsonData = new JSONObject(data);
        Log.d(TAG, "parseJSon:size " + hotpllist.size());
        //route배열 객체를 가져온다.
        JSONArray jsonarrayresults = jsonData.getJSONArray("results");
        Log.d(TAG, "parseJSon:객체의 갯수 " + jsonarrayresults.length());
        //결과를 담는다.
        String result = jsonData.getString("status");
        Log.d(TAG, "parseJSon: jsonData.getString(\"status\") " + jsonData.getString("status"));
        Log.d(TAG, "parseJSon:jsonData.get(\"status\") " + jsonData.get("status"));


        Log.d(TAG, "parseJSon: num"+numrequest);
        //다음 페이징이 있는지 확인한다. 있으면 페이징 토큰을 받는다.
        if (numrequest !=4) {
            pagetoken = jsonData.getString("next_page_token");
            Log.d(TAG, "parseJSon:token " + pagetoken);
        }

        Log.d(TAG, "parseJSon:before start ");
        //  Log.d(TAG, "parseJSon: "+pagetoken);
        if (result.equals("OK")) {
            //경로 결과가 있는 경우
            //길에 대한 배열의 크기만큼 가져온다.
            for (int i = 0; i < jsonarrayresults.length(); i++) {
                //한 식당에 대한 객체를 가져옴
                Log.d(TAG, "parseJSon: i " + i);
                JSONObject jsonresult = jsonarrayresults.getJSONObject(i);
                //식당의 위경도를 가저욤
                Log.d(TAG, "parseJSon: 식당정보전체" + i);
                JSONObject geometry = jsonresult.getJSONObject("geometry");
                Log.d(TAG, "parseJSon: " + geometry);
                Log.d(TAG, "parseJSon: 식당정보위치정보" + i);
                JSONObject location = geometry.getJSONObject("location");
                Log.d(TAG, "parseJSon: 식당정보위경도" + i);
                double lat = location.getDouble("lat");
                Log.d(TAG, "parseJSon: lat" + lat);
                Log.d(TAG, "parseJSon: 식당위도" + i);

                double lng = location.getDouble("lng");
                //식당의 이름과 평점를 가져옴
                Log.d(TAG, "parseJSon: 식당경도" + i);

                Log.d(TAG, "parseJSon: long" + lng);
                String name = jsonresult.getString("name");
                Log.d(TAG, "parseJSon: 식당정보이름" + i);

                double rating = jsonresult.getDouble("rating");
                Log.d(TAG, "parseJSon: 식당정보평가" + i);

                int user_ratings_total = jsonresult.getInt("user_ratings_total");
                Log.d(TAG, "parseJSon: 식당정보평수" + i);

                Log.d(TAG, "parseJSon: " + lat + "\n" + lng + "\n" + name + "\n" + rating + "\n" + user_ratings_total + "\n");
                //장소에 대한 사진 정보를 가져옴
                Log.d(TAG, "parseJSon: 식당정보전체" + i);

                Results results = new Results(lat, lng, name, rating, user_ratings_total);


                hotpllist.add(results);
            }
            Log.d(TAG, "parseJSon: size" + hotpllist.size());
            onsearchSuccess(hotpllist);
        } else {
            onsearchFail();
        }

    }

}
