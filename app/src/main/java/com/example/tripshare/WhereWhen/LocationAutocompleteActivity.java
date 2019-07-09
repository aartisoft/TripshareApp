/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.tripshare.WhereWhen;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.OnedayPlace;
import com.example.tripshare.MainActivity;
import com.example.tripshare.R;
import com.example.tripshare.Trip.DirectionActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.geonames.GeoNamesException;
import org.geonames.WebService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for testing Autocomplete (activity and fragment widgets, and programmatic).
 */
public class LocationAutocompleteActivity extends AppCompatActivity {

  private static final int AUTOCOMPLETE_REQUEST_CODE = 23487;
  private PlacesClient placesClient;
  private FieldSelector fieldSelector;
  private String id;
    private String address;
    private Bitmap bitmap;
    private String countrycode;
    private LatLng latlon;
    private String name,way;
    double latitude,longitude;
  private LottieAnimationView animationView;
    public static ApiInterface apiInterface;
    AlertDialog.Builder builder;


  private static final String TAG = "AutocompleteTestActivit";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.locationautocomplete);
      String apiKey = getString(R.string.places_api_key);
      fieldSelector = new FieldSelector();
      animationView = (LottieAnimationView) findViewById(R.id.reg_lottie);
      apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

//      ActionBar actionBar = getSupportActionBar();
//      assert actionBar != null;
//      actionBar.setDisplayHomeAsUpEnabled(true);

      if (apiKey.equals("")) {
          Toast.makeText(this, getString(R.string.error_api_key), Toast.LENGTH_LONG).show();
          return;
      }

      // Setup Places Client
      if (!Places.isInitialized()) {
          Places.initialize(getApplicationContext(), apiKey);
      }

    Log.d(TAG, "onCreate: setContentView(R.layout.locationautocomplete);");
    // Retrieve a PlacesClient (previously initialized - see StreamingActivity)
    placesClient = Places.createClient(this);
    Log.d(TAG, "onCreate: placesClient = Places.createClient(this);");
    // Set up view objects

    Log.d(TAG, "onCreate: responseView = findViewById(R.id.response)");

    setLoading(false);
    startAutocompleteActivity();

  }


    /**
   * Called when AutocompleteActivity finishes
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
    if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
      if (resultCode == AutocompleteActivity.RESULT_OK) {

        //검색을 버튼이나 엔터를 누를 경우 로딩 시작
       setLoading(true);

       //장소에 대한 정보를 가져옴
        Place place = Autocomplete.getPlaceFromIntent(intent);
        Log.d(TAG, "onActivityResult: place >>"+place);
        place.getLatLng();
          Log.d(TAG, "onActivityResult: latlng "+ place.getLatLng().longitude);
          Log.d(TAG, "onActivityResult: address "+ place.getAddress());
          //사진 업승면 에러나는 듯
          // Log.d(TAG, "onActivityResult: photoMeta.tostring "+ place.getPhotoMetadatas().toString());
//          Log.d(TAG, "onActivityResult: photoMeta 0번 .tostring"+place.getPhotoMetadatas().get(0).toString());
          Log.d(TAG, "onActivityResult: id "+ place.getId());
          Log.d(TAG, "onActivityResult: name "+place.getName());
            id= place.getId(); address =place.getAddress();
            latitude = place.getLatLng().latitude;
            longitude = place.getLatLng().longitude;
            name = place.getName();


          if (getIntent().getStringExtra("way") == null && getIntent().getStringExtra("일정") == null){
              //http통신으로 위도와 경도를 가지고 해당 도시의 나라 코드를 받아온다.
              //해당 나라 코드는 장소 검색할 때 검색 범위 제한에 사용된다.
              new GeoNamesTask().execute(latitude, longitude);
          }else if (getIntent().getStringExtra("일정") == null){
              //http통신으로 위도와 경도를 가지고 해당 도시의 나라 코드를 받아온다.
              //해당 나라 코드는 장소 검색할 때 검색 범위 제한에 사용된다.
              new GeoNamesTask().execute(latitude, longitude);


          }else if (getIntent().getStringExtra("일정").equals("길찾기")) {
              Log.d(TAG, "onActivityResult: 길찾기");
              Intent intentdirection = new Intent(LocationAutocompleteActivity.this, DirectionActivity.class);
              intentdirection.putExtra("orlat", latitude);
              intentdirection.putExtra("orlong", longitude);
              intentdirection.putExtra("orname",name);
              double dslat = getIntent().getDoubleExtra("dslat",0);
              double dslong = getIntent().getDoubleExtra("dslong",0);
              String name = getIntent().getStringExtra("dsname");
              intentdirection.putExtra("dslat",dslat);
              intentdirection.putExtra("dslong",dslong);
              intentdirection.putExtra("dsname",name);
              startActivity(intentdirection);
              finish();

          }else {
              Log.d(TAG, "onActivityResult: 일정 추가를 위한 장소 검색");
                  //일정 추가를 위한 장소 검색인 경우
                  //검색된 장소를 서버에 추가한다.(여행도시번호,장소이름,아이디,위,경도,여행 몇 번째 날인지)
                  int date = getIntent().getIntExtra("언제",0);
                  int tnum = getIntent().getIntExtra("도시",0);
                  int next = getIntent().getIntExtra("다음번호",0);
                  Log.d(TAG, "onActivityResult: date, tnum"+ date+"\n"+tnum+"\n");
                  Call<OnedayPlace> callfirst = apiInterface.Plusmyoneday(tnum, name,id,latitude,longitude,date,next);
                  callfirst.enqueue(new Callback<OnedayPlace>() {
                      @Override
                      public void onResponse(Call<OnedayPlace> call, Response<OnedayPlace> response) {
                          if (response.isSuccessful()){
                              setLoading(false);
                              Log.d(TAG, "onResponse: "+response.body().getResponse());
                              finish();
                          }else {
                              Log.d(TAG, "onResponse: 에러");
                              setLoading(false);
                              finish();
                          }
                      }

                      @Override
                      public void onFailure(Call<OnedayPlace> call, Throwable t) {
                          Log.d(TAG, "onFailure: "+t);
                          setLoading(false);
                          finish();
                      }
                  });
          }

      } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
          //에러발생한 경우
        Status status = Autocomplete.getStatusFromIntent(intent);
        Log.d(TAG, "onActivityResult: error "+status.getStatusMessage());
        finish();

      } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
        // The user canceled the operation.

            if (getIntent().getStringExtra("일정") == null){
                //여행 일정 추가가 아닌 여행 등록,수정인 경우
                Log.d(TAG, "onActivityResult: 취손데 등록수정");
//                Intent gointent = new Intent(getApplicationContext(), StreamingActivity.class);
//                gointent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(gointent);
                finish();
            }else {
                Log.d(TAG, "onActivityResult: 취손데 여행장소 추가");
              //  Intent itiintent = new Intent(getApplicationContext(), ItineraryActivity.class);
                //itiintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //startActivity(itiintent);
                finish();
            }
          Log.d(TAG, "onActivityResult: user canel");

      }else{
          finish();
      }
    }
    super.onActivityResult(requestCode, resultCode, intent);
  }



    //위경도를 가지고 나라 iso코드를 가져온다.
    //장소 검색시 검색 범위를 해당 나라로 제한하기 위해서
    //가져오는 시작과 끝을 애니메이션으로 보여주는 것.
    private class GeoNamesTask extends AsyncTask<Double, Void, String> {


        @Override
        protected String doInBackground(Double... params) {
            return queryGeoNames_countryCode(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String code) {
            Log.d(TAG, "onPostExecute: s" + code);
            String coderesult = code.substring(code.length() - 2);
            if (getIntent().getStringExtra("way") == null && getIntent().getStringExtra("일정") == null) {
                Log.d(TAG, "onPostExecute: 추가인경우");
                //수정이 아닌 추가인 경우
                //일정을 선택하러 감
                setLoading(false);
                Intent howlongintent = new Intent(LocationAutocompleteActivity.this, HowlongActivity.class);
                howlongintent.putExtra("id", id);
                howlongintent.putExtra("name", name);
                howlongintent.putExtra("countrycode", coderesult);
                howlongintent.putExtra("latitude", latitude);
                howlongintent.putExtra("longitude", longitude);
                startActivity(howlongintent);
                finish();
            } else if (getIntent().getStringExtra("일정") == null) {
                Log.d(TAG, "onPostExecute: 수정인 경우");
                //수정인 경우
                //메인화면에서 몇번째 아이템인지 받음
                //여기서는 이 position을 사용하지 않고 메인으로 보낼거다
                int tnum = getIntent().getIntExtra("tnum", 0);
                int term = getIntent().getIntExtra("term", 0);
                Log.d(TAG, "fetchPhoto: tnum term /" + tnum + "\n" + term);

                //다이어로그를 생성해서 일정에 등록된 장소를 유지할지 삭제할지 물어본다.
                //또한 뒤로가기나 배경화면을 클릭했을 경우 취소를 할 수 없게 한다.
                builder = new AlertDialog.Builder(LocationAutocompleteActivity.this);
                builder.setTitle("장소 수정");
                builder.setMessage("일정에 등록된 장소들을 삭제하시겠습니까?");
                builder.setCancelable(false);
                builder.setPositiveButton("삭제", (dialog, which) -> {
                    //기존에 등록된 여행 도시를 수정하고
                    //여행 일정에 등록된 장소를 삭제한다.
                    Call<Trip> call = apiInterface.Editmytrip(tnum, id, name, latitude, longitude, coderesult, term);
                    call.enqueue(new Callback<Trip>() {
                        @Override
                        public void onResponse(Call<Trip> call, Response<Trip> response) {
                            if (response.isSuccessful()) {
                                //데이터 베이스에 장소 이름, id 수정을 완료했으므로
                                //메인화면으로 가기전 성공 여부 알려주기
                                Log.d(TAG, "onResponse: edit success? " + response.body().getResponse());
                                setLoading(false);
                                Intent editintent = new Intent(LocationAutocompleteActivity.this, MainActivity.class);
                                editintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(editintent);
                                finish();
                            } else {
                                Log.d(TAG, "onResponse: 에러 발생");
                                setLoading(false);
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<Trip> call, Throwable t) {
                            Log.d(TAG, "onFailure: " + t);
                            setLoading(false);
                            finish();
                        }
                    });
                })
                        .setNegativeButton("유지", (dialog, which) -> {
                            //여행할 도시나 국가는 변경하지만 기존에 등록한 장소를 유지할 경우
                            int keep = 0;
                            Call<Trip> call = apiInterface.Editmytrip(tnum, id, name, latitude, longitude, coderesult, keep);
                            call.enqueue(new Callback<Trip>() {
                                @Override
                                public void onResponse(Call<Trip> call, Response<Trip> response) {
                                    if (response.isSuccessful()) {
                                        Log.d(TAG, "onResponse:국가는 변경하고 기존 등록 장소 유지 ");
                                        Log.d(TAG, "onResponse: edit success? " + response.body().getResponse());
                                        setLoading(false);
                                        Intent editintent = new Intent(LocationAutocompleteActivity.this, MainActivity.class);
                                        editintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(editintent);
                                        finish();
                                    } else {
                                        Log.d(TAG, "onResponse: 에러 발생");
                                        setLoading(false);
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Trip> call, Throwable t) {
                                    Log.d(TAG, "onFailure: " + t);
                                    setLoading(false);
                                    finish();
                                }
                            });
                        });
            builder.show();
            }
        }
        private String queryGeoNames_countryCode(double latitude, double longitude){
            String queryResult = "";

            /*
            Do not use the 'demo' account for your app or your tests.
            It is only meant for the sample links on the documentation pages.
            Create your own account instead.
             */
            WebService.setUserName("seung");

            try {
                queryResult = "CountryCode: " + WebService.countryCode(latitude, longitude);
                Log.d(TAG, "queryGeoNames_countryCode: quereyresult"+queryResult);
            } catch (IOException e) {
                e.printStackTrace();
                queryResult = e.getMessage();
            } catch (GeoNamesException e) {
                e.printStackTrace();
                queryResult = e.getMessage();
            }

            return queryResult;
        }
    }



//    private void fetchPhoto(PhotoMetadata photoMetadata) {
//        setLoading(true);
//
//        FetchPhotoRequest.Builder photoRequestBuilder = FetchPhotoRequest.builder(photoMetadata);
//        Task<FetchPhotoResponse> photoTask = placesClient.fetchPhoto(photoRequestBuilder.build());
//
//        photoTask.addOnSuccessListener(
//                response -> {
//                    Log.d(TAG, "fetchPhoto:response.getBitmaptoString() "+response.getBitmaptoString());
//                    Log.d(TAG, "fetchPhoto:response "+response);
//                  Log.d(TAG, "fetchPhoto:response "+response.toString());
//
//
//                });
//
//        photoTask.addOnFailureListener(
//                Throwable::printStackTrace);
//
//        photoTask.addOnCompleteListener(response -> setLoading(false));
//    }

  private void startAutocompleteActivity() {

    Log.d(TAG, "startAutocompleteActivity: mode: "+getMode());
    for(int i =0 ; i<getPlaceFields().size() ; i++){
        getPlaceFields().get(i);
        Log.d(TAG, "startAutocompleteActivity: getPlaceFields "+ getPlaceFields().get(i));
    }
      Log.d(TAG, "startAutocompleteActivity: gettype"+getTypeFilter());

      Log.d(TAG, "startAutocompleteActivity: getPlaceFields "+getPlaceFields());

      if (getIntent().getStringExtra("일정") ==null){
          Intent autocompleteIntent =
                  new Autocomplete.IntentBuilder(getMode(), getPlaceFields()) //하드코딩 할 부분
                          .setTypeFilter(getTypeFilter())
                          .build(LocationAutocompleteActivity.this);
          startActivityForResult(autocompleteIntent, AUTOCOMPLETE_REQUEST_CODE);
      }else{

            countrycode =getIntent().getStringExtra("일정");

          Log.d(TAG, "startAutocompleteActivity: getIntent().getStringExtra(\"일정\")"+ countrycode);
          Intent autocompleteIntent =
                  new Autocomplete.IntentBuilder(getMode(), getPlaceFields()) //하드코딩 할 부분
                          .setTypeFilter(getTypeFilter())
               //           .setCountry(countrycode)
                          .build(LocationAutocompleteActivity.this);
          startActivityForResult(autocompleteIntent, AUTOCOMPLETE_REQUEST_CODE);

      }


  }

    //////////////////////////
  // Helper methods below //
  //////////////////////////

    //장소에 대해서 가져올 정보 리스트
    //5개 ID,주소,이미지,이름,위경도
  private List<Place.Field> getPlaceFields() {
      return fieldSelector.getSelectedFields();
  }

  //자동 완성될 검색 범위
  private TypeFilter getTypeFilter() {

      if (getIntent().getStringExtra("일정")==null){
          //여행 추가일 경우 지역,나라가 검색됨
          Log.d(TAG, "getTypeFilter: null");
         return TypeFilter.REGIONS;
      }else{
          //갈 장소 추가이면 지역,나라 검색되는 것이 아니라
          //여행 장소 중심
          Log.d(TAG, "getTypeFilter:not null");
          String itinerary = getIntent().getStringExtra("일정");
          Log.d(TAG, "getTypeFilter: "+itinerary);
          return TypeFilter.ESTABLISHMENT;
      }


  }

  //검색 방식을 배경화면을 메인화면으로 할 것인지 검색창만 풀 스크린 할 것인지
  private AutocompleteActivityMode getMode() {
      //AutocompleteActivityMode.OVERLAY
    return AutocompleteActivityMode.FULLSCREEN;
  }

  private void setLoading(boolean loading) {
      //서버에서 정보를 받을 때는 로딩된다.
    Log.d(TAG, "setLoading: ");

    if (loading){
        //애니메이션 보여주고 움직이게 한다.
      animationView.setVisibility(View.VISIBLE);
      animationView.setAnimation("register.json");
      animationView.playAnimation();
      animationView.loop(true);
      animationView.resumeAnimation();

    }else{
        //애니메이션 멈추고 안보이게 한다.
      animationView.pauseAnimation();
      animationView.setVisibility(View.INVISIBLE);
    }

  }

}
