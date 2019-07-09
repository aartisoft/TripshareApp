package com.example.tripshare.Trip;

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
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tripshare.Adapter.SearchAdapter;
import com.example.tripshare.Data.Results;
import com.example.tripshare.R;
import com.google.android.libraries.places.api.Places;

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

public class SearchActivity extends AppCompatActivity {

    //도시이름, 여행번호, 여행 총 일수, 시작,종료일 에 대한 정보
    private String tstart,tend,placename,type;
    private int term,tnum;
    private ImageView placeimg;
    private String firsturl, url;
    private String pagetoken;
    private static final String REQUEST_URL ="https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBdGjUa3OSXHOInYWbLdBCDnS4pHjtGme8";
    private LottieAnimationView lottieAnimationView;
    //데이터 담길 리스트
    private ArrayList<Results> resultslist;
    int paging;
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private static final String TAG = "SearchActivity";
    String urlOrigin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        tstart = getIntent().getStringExtra("tstart");
        tend = getIntent().getStringExtra("tend");
        placename =getIntent().getStringExtra("placename");
        term = getIntent().getIntExtra("term", 0);
        tnum = getIntent().getIntExtra("tnum",0);
        resultslist = new ArrayList<>();
        //호텔인지, 맛집인지
        type= getIntent().getStringExtra("type");

        //데이터를 더 받기 위해 사용되는 변수
        paging =0;

        lottieAnimationView =findViewById(R.id.lottie_search);
        recyclerView = findViewById(R.id.recyclerview_search);
        //리사이클러뷰 레이아웃 설정
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SearchActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        //마지막 아이템일 경우 데이터를 더 가져오기 위해
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastvisibleitemposition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition()+1;
                int itemTotalCount =recyclerView.getAdapter().getItemCount();
                Log.d(TAG, "onScrolled:last total "+lastvisibleitemposition+"\n"+itemTotalCount);

                if (lastvisibleitemposition ==itemTotalCount){
                    Log.d(TAG, "aonScrolled: "+ paging);

                    if (paging == 0) {
                        try {
                        //2번째로 데이터 받을 때 데이터 받을 땐 0
                            paging = 1;
                            requestrestraunts();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }else if (paging ==2){
                        //3번째로 데이터 받을 땐 paging=2
                        try {
                            paging = 3;
                            requestrestraunts();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "aonScrolled: "+ paging);
                }else if (lastvisibleitemposition ==60){
                    Toast.makeText(SearchActivity.this, "마지막 페이지 입니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Log.d(TAG, "onCreate:받은 데이터 "+tnum+"\n"+tend+"\n"+placename+"\n"+tstart+"\n"+term);

        ActionBar actionBar = getSupportActionBar();

        if (type.equals("맛집")){
            actionBar.setTitle(placename+" 맛집");
        }else {
            actionBar.setTitle(placename+"호텔");
        }
        actionBar.setDisplayHomeAsUpEnabled(true);

        //처음 페이징
        pagetoken ="first";

        try {
            requestrestraunts();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private String createurl() throws UnsupportedEncodingException {

        //맛집인지 호텔 검색인지에 따라 구글에 요청할 url이 달라짐
        if (type.equals("맛집")){
            String query = "restaurants in "+placename;
            urlOrigin = URLEncoder.encode(query, "utf-8");
        }else {
            String query = "hotel in "+placename;
            urlOrigin = URLEncoder.encode(query, "utf-8");
        }

        if (pagetoken.equals("first")) {
            //처음 정보를 가져올때
            url = REQUEST_URL+"query="+urlOrigin+"&key=" + GOOGLE_API_KEY;
        }else{
            //두 번째, 세 번째 가져올 때
            Log.d(TAG, "createurl: pagetoken"+pagetoken);
            url = REQUEST_URL+"query="+urlOrigin+"&pagetoken="+pagetoken+"&key=" + GOOGLE_API_KEY;
        }

        Log.d(TAG, "createurl: "+url);
    return url;
    }

    private void requestrestraunts() throws UnsupportedEncodingException {
        loading(true);
        Log.d(TAG, "requestrestraunts: "+createurl());
        DownloadRawData downloadRawData = new DownloadRawData();
        //맛집 데이터를 주세요! 요청
        downloadRawData.execute(createurl());

    }

    //요청에 성공했을 때
    public void onsearchSuccess(ArrayList<Results> list){
        Log.d(TAG, "aonsearchSuccess: paging"+ paging);
        if (paging ==1){

            //21~40번이 추가됨
            paging =2;
            adapter = new SearchAdapter(getApplicationContext(), list);
            adapter.notifyItemRangeInserted(20,39);
            recyclerView.setAdapter(adapter);
        }else if (paging ==3){
            //2번째로 데이터가 올 때
            //41~60
            paging =4;
            adapter = new SearchAdapter(getApplicationContext(), list);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
        }else {
            //처음에 들어왔을 때
            adapter = new SearchAdapter(getApplicationContext(), list);
            recyclerView.setAdapter(adapter);

        }
        loading(false);
        Log.d(TAG, "aonsearchSuccess: paging"+ paging);

    }

    //여러 이유로 요청에 실패했을 때 권한이나, 검색결과가 없거나
    public void onsearchFail(){
        loading(false);
        if (paging == 1){
            //2번째 요청이 실패
            paging =0;
        }else if (paging ==3){
            //3번째 요청이 실패
            paging =2;
        }

    }
    //데이터를 받아올 때 로딩이 실행 되고 총 3번 실행된다.
    private void loading(boolean load){

        if (load){
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.setAnimation("register.json");
            lottieAnimationView.playAnimation();
            lottieAnimationView.loop(true);
        }else {
            lottieAnimationView.pauseAnimation();
            lottieAnimationView.setVisibility(View.INVISIBLE);
        }

    }
    class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //url을 받아온다.
            Log.d(TAG, "doInBackground: params : "+params.toString());
            Log.d(TAG, "doInBackground: params[0] : "+params[0]);
            String link = params[0];
            try {
                URL url = new URL(link);
                Log.d(TAG, "doInBackground: url /"+url);
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
        //길찾은 내용이 없으면 바로 끝냄
        if (data == null)
            return;

        Log.d(TAG, "parseJSon: Data "+data);

        //서버에게 받은 raw한 모든 내용을 대한 문자열을
        //json으로 만든다.
        JSONObject jsonData = new JSONObject(data);
        Log.d(TAG, "parseJSon:size "+resultslist.size());
        //route배열 객체를 가져온다.
        JSONArray jsonarrayresults = jsonData.getJSONArray("results");
        Log.d(TAG, "parseJSon:객체의 갯수 "+jsonarrayresults.length());
        //결과를 담는다.
        String result = jsonData.getString("status");
        Log.d(TAG, "parseJSon: jsonData.getString(\"status\") "+jsonData.getString("status"));
        Log.d(TAG, "parseJSon:jsonData.get(\"status\") "+jsonData.get("status"));

        //다음 페이징이 있는지 확인한다.
        if (paging != 3){
            pagetoken = jsonData.getString("next_page_token");
            Log.d(TAG, "parseJSon:token "+pagetoken);
        }



        Log.d(TAG, "parseJSon:before start ");
      //  Log.d(TAG, "parseJSon: "+pagetoken);
        if (result.equals("OK")){
            //경로 결과가 있는 경우
            //길에 대한 배열의 크기만큼 가져온다.
            for (int i = 0; i < jsonarrayresults.length(); i++) {
                //한 식당에 대한 객체를 가져옴
                Log.d(TAG, "parseJSon: i "+i);
                JSONObject jsonresult = jsonarrayresults.getJSONObject(i);
                //식당의 위경도를 가저욤
                Log.d(TAG, "parseJSon: 식당정보전체"+i);
                JSONObject geometry = jsonresult.getJSONObject("geometry");
                Log.d(TAG, "parseJSon: "+geometry);
                Log.d(TAG, "parseJSon: 식당정보위치정보"+i);
                JSONObject location = geometry.getJSONObject("location");
                Log.d(TAG, "parseJSon: 식당정보위경도"+i);
                double lat = location.getDouble("lat");
                Log.d(TAG, "parseJSon: lat"+lat);
                Log.d(TAG, "parseJSon: 식당위도"+i);

                double lng = location.getDouble("lng");
                //식당의 이름과 평점를 가져옴
                Log.d(TAG, "parseJSon: 식당경도"+i);

                Log.d(TAG, "parseJSon: long"+lng);
                String name = jsonresult.getString("name");
                Log.d(TAG, "parseJSon: 식당정보이름"+i);

                double rating  =  jsonresult.getDouble("rating");
                Log.d(TAG, "parseJSon: 식당정보평가"+i);

                int user_ratings_total = jsonresult.getInt("user_ratings_total");
                Log.d(TAG, "parseJSon: 식당정보평수"+i);

                Log.d(TAG, "parseJSon: "+lat+"\n"+lng+"\n"+name+"\n"+rating+"\n"+user_ratings_total+"\n");
//                //장소에 대한 사진 정보를 가져옴
                Log.d(TAG, "parseJSon: 식당정보전체"+i);

//                JSONArray photos = jsonresult.getJSONArray("photos");
//                Log.d(TAG, "parseJSon:photos.getJSONObject(0).getInt(\"height\") "+photos.getJSONObject(0).getInt("height"));
//                Log.d(TAG, "parseJSon:photos.getJSONObject(0).getInt(\"width\") "+photos.getJSONObject(0).getInt("width"));
//                Log.d(TAG, "parseJSon:photos.getJSONObject(0).getJSONArray(\"html_attributions\").getString(0) "+photos.getJSONObject(0).getJSONArray("html_attributions").getString(0));
//                Log.d(TAG, "parseJSon:photos.getJSONObject(0).getString(\"photo_reference\") "+photos.getJSONObject(0).getString("photo_reference"));
//                PlacePhoto placePhoto = new PlacePhoto(photos.getJSONObject(0).getInt("height"),photos.getJSONObject(0).getInt("width")
//                        ,photos.getJSONObject(0).getJSONArray("html_attributions").getString(0), photos.getJSONObject(0).getString("photo_reference"));
//                Log.d(TAG, "parseJSon:photo "+placePhoto);
//                //한 식당에 대한 정보를 모두 가진 결과 객체를 만듬
                Results results = new Results(lat,lng,name, rating, user_ratings_total);


                resultslist.add(results);
            }
            Log.d(TAG, "parseJSon: size"+resultslist.size());
            onsearchSuccess(resultslist);
        }else{
            onsearchFail();
        }

    }



    @Override //앱바 만드는 곳
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.triptool, menu);
        return true;
    }

    @Override   //앱바에서 지도, 뒤로가기 클릭할 때
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.map :
                Log.d(TAG, "onOptionsItemSelected: 맵이 클릭됨");
                Intent intent = new Intent(SearchActivity.this, MapsActivity.class);
                intent.putExtra("resultslist", resultslist);
                intent.putExtra("latitude", getIntent().getDoubleExtra("latitude",0));
                intent.putExtra("longitude", getIntent().getDoubleExtra("longitude",0));
                intent.putExtra("tnum",tnum);
                intent.putExtra("term",term);
                intent.putExtra("placename",placename);
                intent.putExtra("tstart",tstart);
                intent.putExtra("tend",tend);
                startActivity(intent);

                return true;
            case android.R.id.home:
                //메인으로
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
