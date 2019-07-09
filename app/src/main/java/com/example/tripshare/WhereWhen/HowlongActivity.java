package com.example.tripshare.WhereWhen;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.MainActivity;
import com.example.tripshare.R;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.squareup.timessquare.CalendarPickerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.squareup.timessquare.CalendarPickerView.SelectionMode.RANGE;

public class HowlongActivity extends AppCompatActivity {
    List dates;
    ArrayList ymdate;
    CalendarPickerView calendar;
    Calendar nextYear, calSelected;

    Button done;
    Date today;
    private double latitude, longitude;
    private String locationid, email, tstart, tend, name,countrycode;
    private Integer howlong;
    private static final String TAG = "HowlongActivity";
    private int tnum, position;

    private PlacesClient placesClient;
    private FieldSelector fieldSelector;
    public static PrefConfig prefConfig;
    public static ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_howlong);


        //쉐어드 생성을 위해 반복 코드 줄이고
        prefConfig = new PrefConfig(this);
        // api 객체를 만든다.
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        // Retrieve a PlacesClient (previously initialized - see StreamingActivity)
//        placesClient = Places.createClient(this);

        fieldSelector = new FieldSelector();

        if (getIntent().getStringExtra("way") != null) {
            //수정인 경우
            tnum = getIntent().getIntExtra("tnum", 0);
            position = getIntent().getIntExtra("position", -1);
            name = getIntent().getStringExtra("placename");
        } else {
            //추가인 경우
            countrycode = getIntent().getStringExtra("countrycode");
            locationid = getIntent().getStringExtra("id");
            name = getIntent().getStringExtra("name");
            latitude = getIntent().getDoubleExtra("latitude",0);
            longitude = getIntent().getDoubleExtra("longitude",0);
        }
        //사용자가 선택한 장소 id
        //취소하고 다른 장소를 선택해도 id값이 그대로인지 확인해보자.

        Log.d(TAG, "onCreate: id " + locationid);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(name);

        //사용자가 기간 선택 후 누를 버튼
        done = findViewById(R.id.done_button);

        //기본은 등록 버튼 선택할 수 없다.
        enableClickButton(false);

        //달력은 오늘 부터 1년 이후까지만 나오게 한다.
        nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        today = new Date();

        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        //캘린더를 오늘을 기준으로 다음날 부터 선택할 수 있게하고
        //두 개의 날짜를 선택하면 그 사이의 기간도 자동 선택되게 했다.
        calendar.init(today, nextYear.getTime())
                .inMode(RANGE)
                .withSelectedDate(today);
        ymdate = new ArrayList();
        //달력에서 날짜를 선택했을 경우
        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                ymdate = new ArrayList();
                //사용자가 설정한 날짜 범위
                Log.d(TAG, "onDateSelected:calendar.getSelectedDates(); " + calendar.getSelectedDates().size());
                dates = calendar.getSelectedDates();

                Log.d(TAG, "onDateSelected: dates= calendar.getSelectedDates();  " + dates.size());
                //연월일을 나눠서 알려줄 수 있는 객체
                calSelected = Calendar.getInstance();

                //여러 날짜를 선택했을 경우
                if (dates.size() >= 2) {
                    //설정 버튼 클릭 가능하게


                    done.setEnabled(true);

                    for (int i = 0; i < dates.size(); i++) {
                        //연,월,일이 같이 있는 dates 객체를 연,월,일 각각 빼기 위해
                        // 캘린더 객체어 넣는다.
                        calSelected.setTime((Date) dates.get(i));
                        //연.월.일
                        String selectedDate = calSelected.get(Calendar.YEAR) + "."
                                + (calSelected.get(Calendar.MONTH) + 1) + "."
                                + calSelected.get(Calendar.DAY_OF_MONTH);
                        //리스트에 넣는다. 연,월,일 순서대로 넣는다.
                        ymdate.add(i, selectedDate);

                        Log.d(TAG, "selected: " + selectedDate);
                        Log.d(TAG, "list index: " + i + "date: " + dates.get(i));
                        Log.d(TAG, "onDateSelected: ymdate index: " + i + " value :" + ymdate.get(i));
                    }

                    Log.d(TAG, "onDateSelected: dates.size() " + dates.size());
                    Log.d(TAG, "onDateSelected: dates " + ymdate.size());
                    Log.d(TAG, "onDateSelected: " + ymdate.get(0) + " ~ " + ymdate.get(dates.size() - 1));
                    done.setText(ymdate.get(0) + " ~ " + ymdate.get(dates.size() - 1) + " (" + dates.size() + "일) / 등록 완료");
                    // Log.d(TAG, "onDateSelected: dates.size() "+nowdatesize);
                    calendar.getSelectedDates();

                    howlong = dates.size();
                    tstart = ymdate.get(0).toString();
                    tend = ymdate.get(dates.size() - 1).toString();


                    Log.d(TAG, "onDateSelected: howlong " + howlong);
                    Log.d(TAG, "onDateSelected: dates " + dates.size());
                    dates.clear();
                } else {
                    //날짜를 한개 이하를 선택하면 등록 버튼 클릭 못하게
                    enableClickButton(false);
                }
            }

            @Override
            public void onDateUnselected(Date date) {
                enableClickButton(false);
            }
        });

        //선택하면 안되는 날들을 선택했을 경우에
        //예) 어제 날짜
        calendar.setOnInvalidDateSelectedListener(date -> {
            enableClickButton(false);
            Toast.makeText(HowlongActivity.this, "선택할 수 없는 날짜입니다.", Toast.LENGTH_SHORT).show();
        });

        //등록 버튼을 클릭 했을 경우
        done.setOnClickListener(v -> {

            email = prefConfig.readEmail();

            if (getIntent().getStringExtra("way") != null) {
                //수정인 경우

                AlertDialog.Builder builder = new AlertDialog.Builder(HowlongActivity.this);
                builder.setTitle("기간 변경");
                builder.setMessage("일정이 줄어드는 경우 없어진 날짜에 등록된 장소들이 삭제됩니다."+"\n"+"일정을 변경하시겠습니까?");
                builder.setPositiveButton("수정", (dialog, which) -> {
                    Call<Trip> editcall = apiInterface.Editmytripterm(tnum, tstart, tend, howlong);
                    editcall.enqueue(new Callback<Trip>() {
                        @Override
                        public void onResponse(Call<Trip> call, Response<Trip> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "onResponse: 성공 했다구 ~!" + response.body().getResponse());

                                Intent editintent = new Intent(HowlongActivity.this, MainActivity.class);
                                editintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(editintent);
                                finish();
                            } else {
                                Log.d(TAG, "onResponse: 에러");
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<Trip> call, Throwable t) {
                            finish();
                            Log.d(TAG, "onFailure: 수정할 때 실패 " + t);
                        }
                    });
                }).setNegativeButton("취소", ((dialog, which) -> {
                    Intent editintent = new Intent(HowlongActivity.this, MainActivity.class);
                    editintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(editintent);
                    finish();
                }));
            builder.show();
            } else {
                //추가인 경우

                Log.d(TAG, "onCreate: " + locationid + "\n" + tstart + "\n" + tend + "\n" + howlong + "\n" + email);
                Call<Trip> call = apiInterface.Plusmytrip(name, locationid, tstart, tend, howlong, email, latitude,longitude,countrycode);
                call.enqueue(new Callback<Trip>() {
                    @Override
                    public void onResponse(Call<Trip> call, Response<Trip> response) {

                        if (response.isSuccessful()) {
                            Trip trip = response.body();
                            Log.d(TAG, "onResponse: " + trip.getTnum());
                            Log.d(TAG, "onResponse: locid " + trip.getLocationid());
                            Log.d(TAG, "onResponse: email " + trip.getEmail());
                            Log.d(TAG, "onResponse: howlong " + trip.getHowlong());
                            Log.d(TAG, "onResponse: te " + trip.getTend());
                            Log.d(TAG, "onResponse: Ts " + trip.getTstart());
                            Log.d(TAG, "onResponse: " + trip);
                            Log.d(TAG, "onResponse: res " + trip.getResponse());
                            Log.d(TAG, "onResponse: lati " + trip.getLatitude());
                            Log.d(TAG, "onResponse: logi " + trip.getLongitude());
                            Log.d(TAG, "onResponse: res " + response.toString());
                            int tnum = trip.getTnum();

                            //메인으로 이동하고 지금 화면 종료료
                            Intent intent = new Intent(HowlongActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

//                            intent.putExtra("tnum", tnum);
//                            intent.putExtra("placename", name);
//                            intent.putExtra("placeid", locationid);
//                            intent.putExtra("howlong", howlong);
//                            intent.putExtra("startdate", tstart);
//                            intent.putExtra("enddate", tend);
//                            intent.putExtra("way", "추가");
//
                        } else {
                            Log.d(TAG, "onResponse: error");

                        }
                    }

                    @Override
                    public void onFailure(Call<Trip> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t);
                    }
                });
            }


            // fetchPlace();
        });

    }

    private void enableClickButton(boolean enable) {


        if (enable) {

        } else {
            done.setText("일정을 선택해 주세요.");
            done.setEnabled(false);
        }


    }
}



//    //장소에 대한 사진 가져오기
//        private void fetchPlace() {
//
//
//            //사진 가져올 건지
//            final boolean isFetchPhotoChecked = true;
//            //장소의 어떤 부분을 가져올 건지 field
//            List<OnedayPlace.Field> placeFields = getPlaceFields();
//
//            if (!validateInputs(isFetchPhotoChecked, placeFields)) {
//                return;
//                //사진을 없다면 밑에 있는 코드는 종료
//            }
//            //사진을 가져올 경우에만 이 밑에 코드 실행해서
//            //로딩시작
//            //  setLoading(true);
//
//            //장소 아이디와 가져올 field list를 통해 장소에 대한 정보를 요청함
//            FetchPlaceRequest request = FetchPlaceRequest.newInstance(getPlaceId(), placeFields);
//            Task<FetchPlaceResponse> placeTask = placesClient.fetchPlace(request);
//
//            //가져오기에 성공했을 때
//            placeTask.addOnSuccessListener(
//                    (response) -> {
//                        //텍스트 뷰에
//                        Log.d(TAG, "fetchPlace: "+response.getPlace().getName());
//                        Log.d(TAG, "fetchPlace: "+response.getPlace().getAddress());
//                        Log.d(TAG, "fetchPlace: "+response.getPlace().getLatLng().longitude);
//                        Log.d(TAG, "fetchPlace: "+response.getPlace().getPhotoMetadatas().get(0));
//
//
//                    });
//
//            placeTask.addOnFailureListener(
//                    (exception) -> {
//                        exception.printStackTrace();
//                        Log.d(TAG, "fetchPlace: "+exception.getMessage());
//                    });
//
//            placeTask.addOnCompleteListener(response -> Log.d(TAG, "fetchPlace: 성공")
//                    //setLoading(false)
//                    );
//        }




//        private boolean validateInputs(
//         boolean isFetchPhotoChecked, List<OnedayPlace.Field> placeFields) {
//            if (isFetchPhotoChecked) {
//
//                if (!placeFields.contains(OnedayPlace.Field.PHOTO_METADATAS)) { //가져올 사진이 없을 때
//                    return false;
//                }
//            }
////사진을 나중에 가져올 거라는 true값
//            return true;
//        }
//
//        private String getPlaceId() {
//            return  locationid;
//        }
//        //가져올 파일들
//        private List<OnedayPlace.Field> getPlaceFields() {
//            return fieldSelector.getSelectedFields();
//        }





//        private void setLoading(boolean loading) {
//            findViewById(R.id.loading).setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
//        }

