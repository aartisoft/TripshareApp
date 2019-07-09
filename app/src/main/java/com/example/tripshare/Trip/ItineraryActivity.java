package com.example.tripshare.Trip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tripshare.Adapter.ItineraryRCVAdapter;
import com.example.tripshare.Adapter.StartDragListener;
import com.example.tripshare.Adapter.SwipeToDeleteCallback;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.Markerlatlong;
import com.example.tripshare.Data.OnedayPlace;
import com.example.tripshare.Data.PlaceList;
import com.example.tripshare.R;
import com.example.tripshare.WhereWhen.LocationAutocompleteActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItineraryActivity  extends FragmentActivity implements View.OnClickListener, StartDragListener,OnMapReadyCallback
, MydialogFragment.MydialogListener {

    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 22;
    private Context mcontext;
    private TabLayout mTablayout;
    private RecyclerView recyclerView;
    ItineraryRCVAdapter itineraryRCVAdapter;
    private FloatingActionButton plusfloating, mapfloating;
    ArrayList<OnedayPlace> onedayPlaceArrayList;
    private int term, tnum, day,next, date;
    private String placename,placeid;
    private static final String TAG = "ItineraryActivity";
    ItemTouchHelper itemTouchhelper;
    private double latitude,longitude;
    private String country,countrycode;
    private TextView requesttv;
    public static ApiInterface apiInterface;
    Marker beforemarker;
    int listposition;
    String item;
    private GoogleMap mMap;
    LatLng position;
    int marknum;
    private TextView markertext;
    private View view;
    private ItineraryActivity activity;
    private String beforeplace;
    private String updateplace;
    SupportMapFragment mapFragment;
    public static String dialogcancel = "yes";
    private int updatelistsize;
    private int tabposition;
    private boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itineray);
        //등록된 장소가 날짜마다 1개도 없을 경우
        //일정 추가할 때랑 날짜 탭 이동할 때 사용됨
        next =0;
        //toultrip에서 받은 여행 도시에 대한 정보
        placeid = getIntent().getStringExtra("placeid");
        placename= getIntent().getStringExtra("placename");
        countrycode= getIntent().getStringExtra("countrycode");
        latitude= getIntent().getDoubleExtra("latitude",0);
        longitude= getIntent().getDoubleExtra("longitude",0);
        term = getIntent().getIntExtra("term", -1);
        tnum = getIntent().getIntExtra("tnum", -1);
        tabposition = getIntent().getIntExtra("tabat", -1);
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);


        Log.d(TAG, "onCreate: "+latitude+"\n"+longitude);
        mTablayout = findViewById(R.id.tab_itinerary);

        //탭의 개수는 여행 총 일수
        for (int i = 1 ; i <= term ; i++ ){
            mTablayout.addTab(mTablayout.newTab().setText("Day "+i));
        }
        Log.d(TAG, "onCreate:now tabposition "+tabposition);
        if (tabposition != -1){
            tabposition = tabposition -1;
            TabLayout.Tab tab =mTablayout.getTabAt(tabposition);
            assert tab != null;
            tab.select();
        }

        requesttv = findViewById(R.id.request_TextView_Itinerary);
        plusfloating = findViewById(R.id.plus_floating_itinerary);
        plusfloating.setOnClickListener(this);
        recyclerView = findViewById(R.id.recyclerView_itinerary);
        requesttv.setVisibility(View.INVISIBLE);
        //액션바
//        ActionBar actionBar = getActionBar();
//        assert actionBar != null;
//        actionBar.setTitle("여행 일정");
//        actionBar.setDisplayHomeAsUpEnabled(true);
        Log.d(TAG, "onTabSelected: what tab? "+mTablayout.getSelectedTabPosition());
        mcontext = getApplicationContext();

  //      populateRecyclerView();
        Log.d(TAG, "onCreate: enableSwipeToDeleteAndUndo() end");

        //내가 만든 마커, 장소 순서를 사용자에게 알려주기 위해
        view = LayoutInflater.from(this).inflate(R.layout.marker,null);
        markertext = (TextView)view.findViewById(R.id.loca_marker);
        markertext.setBackgroundResource(R.drawable.threebluecicle);
        mTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Givemyoneday();
                Log.d(TAG, "onTabSelected: what tab? "+mTablayout.getSelectedTabPosition());
                Log.d(TAG, "onTabSelected: what tab? "+tab.getPosition());
                //이동 전에 몇일 차인지 알려줌
                }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //밑은 선택된 텝이 무슨텝인지 알려줌
                Log.d(TAG, "onTabUnselected: "+mTablayout.getSelectedTabPosition());
                //tab.getposition은 탭 이동전에 무슨텝인지 알려주는거
                Log.d(TAG, "onTabUnselected: "+tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabReselected: ");
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        getLocationPermission();
        //장소 리스트를 받아서 리사이클러뷰에 넣는다.
        Givemyoneday();

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        if (mMap != null){
            mMap.clear();
        }
        mMap = googleMap;
        //확대 축소할 수 있는 벌튼 활성화
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //내 위치 가져오는 권한 설정과 버튼 활서오하
        updateLocationUI();
        //마커를 찍기
        getmarker();
    }

    public void getmarker() {
        //마크 클릭했을 때 보여줄 순서를 초기화 해준다.
        marknum = 0;
        //장소들의 위,경도,장소이름 데이터를 가질 리스트
        ArrayList<Markerlatlong> latlongList = new ArrayList<>();
        //장소들의 위도만 가질 리스트
        List latlist = new ArrayList();
        //장소들의 경도만 가질 리스트
        List longlist = new ArrayList();
        //장소에 대한 여러 정보를 가지고 있는 리스트를 가져와 위,경도만 갖는 리스트로 바꿔줌
        for (int i =0 ; i< onedayPlaceArrayList.size() ; i++){
            latlongList.add(new Markerlatlong(onedayPlaceArrayList.get(i).getLatitude(),onedayPlaceArrayList.get(i).getLongitude(), onedayPlaceArrayList.get(i).getPlacename()));
            latlist.add(onedayPlaceArrayList.get(i).getLatitude());
            longlist.add(onedayPlaceArrayList.get(i).getLongitude());
        }

        Log.d(TAG, "getmarker: latlong size"+latlongList.size());
        Log.d(TAG, "getmarker: oneday size"+onedayPlaceArrayList.size());
        if (onedayPlaceArrayList.size() != 0){
            //장소가 하나라도 있을 경우
            Log.d(TAG, "getmarker: latmax= "+ Collections.max(latlist));
            Log.d(TAG, "getmarker: latmin="+ Collections.min(latlist));
            Log.d(TAG, "getmarker: longmax="+Collections.max(longlist));
            Log.d(TAG, "getmarker: longmin="+Collections.min(longlist));
            double maxlat = (double) Collections.max(latlist);
            double maxlong = (double) Collections.max(longlist);
            double minlat = (double) Collections.min(latlist);
            double minlong = (double) Collections.min(longlist);

            LatLngBounds focus = new LatLngBounds(new LatLng(minlat,minlong), new LatLng(maxlat, maxlong));
            //마커를 추가해줌

            for (Markerlatlong markerlatlong: latlongList){
                //marknum은 커스텀 마크에 붙일 장소 순서
                marknum = marknum+1;
                 beforemarker = addMarker(markerlatlong);
                Log.d(TAG, "getmarker:size ");
            }
            //마커간의 선을 그려줄 option객체
            PolylineOptions polylineOptions = new PolylineOptions();
            //마커간의 선을 하나씩 넣어줌
            for (Markerlatlong markerlatlong: latlongList){
                position = new LatLng(markerlatlong.getLatitude(),markerlatlong.getLongitude());
                polylineOptions.add(position);

            }

            //넣은 선을 지도에서 보여줌
            Polyline mpolyline = mMap.addPolyline(polylineOptions);
            //넣을 선의 형태를 변경해줌
            stylePolygon(mpolyline);

            if (latlongList.size() ==1){
                Log.d(TAG, "getmarker: 1개");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latlongList.get(0).getLatitude(), latlongList.get(0).getLongitude()), 10));
            }else{
                Log.d(TAG, "getmarker: 1개 이상");
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(focus, 100));
            }

        }else{
            Log.d(TAG, "getmarker: 장소 0개");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 5));
        }

    }

    // View를 Bitmap으로 변환
    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public void stylePolygon(Polyline mpolyline) {
        //시작을 동그랗게
        mpolyline.setStartCap(new RoundCap());
        //끝도 동그랗게
        mpolyline.setEndCap(new RoundCap());
        //선의 너비를 정함
        mpolyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        //색깔도 정함
        mpolyline.setColor(COLOR_ORANGE_ARGB);
        //선이 만나는 지점을 동그랗게
        mpolyline.setJointType(JointType.ROUND);
    }

   public Marker addMarker(Markerlatlong markerlatlong) {

        Log.d(TAG, "addMarker: markerlatlong 횟수 "+markerlatlong.getName());
        //장소 하나당 마커 하나 생성하고 생성할 때 위치랑 이름 지정
        LatLng position = new LatLng(markerlatlong.getLatitude(), markerlatlong.getLongitude());

        Log.d(TAG, "addMarker: "+String.valueOf(marknum));
       markertext.setText(String.valueOf(marknum));

       markertext.setTextColor(Color.WHITE);

        //마커를 넣어줄 Option 객체
        MarkerOptions markerOptions = new MarkerOptions();
        //마커에서 보여질 이름
        markerOptions.title(markerlatlong.getName());

        //마커의 위치
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this,view)));
             return mMap.addMarker(markerOptions);
    }


    private void Givemyoneday() {
        date = mTablayout.getSelectedTabPosition()+1;
        Log.d(TAG, "Givemyoneday:tabposition "+mTablayout.getSelectedTabPosition());
        Log.d(TAG, "Givemyoneday:date "+date);
        Log.d(TAG, "Givemyoneday:tnum "+tnum);
        Call<PlaceList> callfirst = apiInterface.Givemyoneday(tnum, date);
        callfirst.enqueue(new Callback<PlaceList>() {
            @Override
            public void onResponse(Call<PlaceList> call, Response<PlaceList> response) {
                if (response.isSuccessful()){
                    Log.d(TAG, "onResponse: success real?: "+response.body().getResponse());
                    makemyPlaceList(response.body().getPlaceArrayList());
                }else {
                    Log.d(TAG, "onResponse: error");
                }
            }
            @Override
            public void onFailure(Call<PlaceList> call, Throwable t) {
                Log.d(TAG, "onFailure: failed"+t);
            }
        });

    }

    private void makemyPlaceList(ArrayList<OnedayPlace> placeArrayList) {

//       Log.d(TAG, "makemyPlaceList: get(0) "+placeArrayList.get(0).getPlacename());
//       Log.d(TAG, "makemyPlaceList:size "+placeArrayList.size());
//        onedayPlaceArrayList=placeArrayList;
//      Log.d(TAG, "makemyPlaceList: "+onedayPlaceArrayList.size());
        onedayPlaceArrayList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_itinerary);
        if (placeArrayList.size() ==0){
            //등록된 일정이 없을 경우  일정을 추가해달라는 텍스트 보여주기
            requesttv.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }else if (placeArrayList.get(0).getPlacename() == null){
            requesttv.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            onedayPlaceArrayList = placeArrayList;
            //지금 몇 개의 장소가 있는지
            //언제 사용되? 스와이프로 삭제할 때
            Log.d(TAG, "makemyPlaceList: "+onedayPlaceArrayList.size());
            day = onedayPlaceArrayList.size();

            requesttv.setVisibility(View.INVISIBLE);
            //등록된 일정이 1개 이상일 경우 등록된 일정 전부를 리사이클러뷰 안에 보여주기

            itineraryRCVAdapter = new ItineraryRCVAdapter(placeArrayList,this,mcontext);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ItineraryActivity.this);

            recyclerView.setLayoutManager(layoutManager);

            enableSwipeToDeleteAndUndo();

        }
        //등록한 자료가 있든 없든
        //구글맵 보여주기
        getmap();
    }

    public void getmap() {
        Log.d(TAG, "getmap: ");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_itinerary);
        mapFragment.getMapAsync(this);
    }

    @Override//앱바를 인플레이트하는 곳.
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.itinerary, menu);
        return true;
    }
   /* private void populateRecyclerView() {
        Log.d(TAG, "populateRecyclerView: ");
        stringArrayList.add("Item 1");
        stringArrayList.add("Item 2");
        stringArrayList.add("Item 3");
        stringArrayList.add("Item 4");
        stringArrayList.add("Item 5");
        stringArrayList.add("Item 6");
        stringArrayList.add("Item 7");
        stringArrayList.add("Item 8");
        stringArrayList.add("Item 9");
        stringArrayList.add("Item 10");
        Log.d(TAG, "mAdapter = new ");
        itineraryRCVAdapter = new ItineraryRCVAdapter(stringArrayList, this);
        Log.d(TAG, "setAdapter ");



    }
*/
    private void enableSwipeToDeleteAndUndo() {
        Log.d(TAG, "enableSwipeToDeleteAndUndo: ");
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this, itineraryRCVAdapter) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                Log.d(TAG, "onSwiped: i : "+i);
                listposition = viewHolder.getAdapterPosition();
                Log.d(TAG, "onSwiped: position :"+position);
                item = itineraryRCVAdapter.getData().get(listposition).getPlacename();

                //다이어로그 연다.
                FragmentManager fm = getSupportFragmentManager();
                DialogFragment dialogFragment = new MydialogFragment();
                dialogFragment.setCancelable(false);
                dialogFragment.show(fm, "delete_fragment_dialog");



//                Log.d(TAG, "onSwiped:name and order "+item+"\n"+itineraryRCVAdapter.getData().get(position).getNumorder());
//                Log.d(TAG, "onSwiped: before size : "+onedayPlaceArrayList.size());
//                Log.d(TAG, "onSwiped:madapter.removeItem(position) day"+day);
//                Log.d(TAG, "onSwiped:madapter.removeItem(position) tnum date"+tnum+"\n"+date);
//                snackbar.setAction("UNDO", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Log.d(TAG, "onClick: mAdapter.restoreItem(item, position)");
//                        itineraryRCVAdapter.restoreItem(item, position);
//                        Log.d(TAG, "scrollToPosition(position)");
//                        recyclerView.scrollToPosition(position);
//                    }
//                });
            }
        };
        Log.d(TAG, "enableSwipeToDeleteAndUndo: new ItemTouchHelper(swipeToDeleteCallback) ");
        itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        Log.d(TAG, "enableSwipeToDeleteAndUndo:attachToRecyclerView(recyclerView) ");
        itemTouchhelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(itineraryRCVAdapter);
    }

    @Override
    public void myCallback(DialogFragment dialogFragment,int result) {
        dialogcancel = "not cancel";
        Log.d(TAG, "myCallback: result"+ result);
        if (result ==1){
            //삭제인 경우
            //여행 장소 리스트 에서 지운다.
            itineraryRCVAdapter.removeItem(listposition);
            //서버에 저장된 요일에서도 지운다.
            int deminishday = day-1;
            Log.d(TAG, "myCallback: listposition "+1);
            int numorder=  listposition+1;
            beforeplace = "";
            updateplace = "";

            if (onedayPlaceArrayList.size() !=0){
            //한 개이상의 장소가 있다면 순서를 다시 정렬하기 위해 고유번호를 가져온다.
            for (int i =0; i<onedayPlaceArrayList.size();i++){
                beforeplace = beforeplace+onedayPlaceArrayList.get(i).getPorder()+".";
                }
                updateplace = beforeplace.substring(0,beforeplace.length()-1);
                Log.d(TAG, "myCallback: updateplace "+updateplace);

            }
            updatelistsize = onedayPlaceArrayList.size();
            Log.d(TAG, "myCallback: tnum, date, day"+tnum+"\n"+date+"\n"+deminishday);
            Log.d(TAG, "myCallback:size "+onedayPlaceArrayList.size());

            Call<OnedayPlace> call = apiInterface.DeletemyonePlace(tnum, date,numorder,updateplace,updatelistsize);
            call.enqueue(new Callback<OnedayPlace>() {
                @Override
                public void onResponse(Call<OnedayPlace> call, Response<OnedayPlace> response) {
                    if (response.isSuccessful()){
                        Log.d(TAG, "onResponse: deleted "+response.body().getResponse());
                        Log.d(TAG, "onResponse:after deleted size "+onedayPlaceArrayList.size());
                        makemyPlaceList(onedayPlaceArrayList);
                    }else {
                        Log.d(TAG, "onResponse: error");
                    }
                }

                @Override
                public void onFailure(Call<OnedayPlace> call, Throwable t) {
                    Log.d(TAG, "onFailure: "+t);
                }
            });

            //제거 되었다는 메세지
            Snackbar snackbar = Snackbar
                    .make(recyclerView, "해당 장소가 여행 일정에서 제거되었습니다.", Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
            Log.d(TAG, "onSwiped:snackbar.show() ");

        }else {
            dialogcancel = "yes cancel";
//            itineraryRCVAdapter.restoreItem(item, listposition);
//            itineraryRCVAdapter.removeItem(listposition);
            Log.d(TAG, "scrollToPosition(position)");
            recyclerView.scrollToPosition(listposition);
        }
        dialogFragment.dismiss();
       // itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        Log.d(TAG, "enableSwipeToDeleteAndUndo:attachToRecyclerView(recyclerView) ");
        itemTouchhelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(itineraryRCVAdapter);
    }
    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG, "requestDrag: viewholder "+viewHolder);
        itemTouchhelper.startDrag(viewHolder);
    }

    @Override
    public void afterDrag() {
        Log.d(TAG, "afterDrag: ");
        makemyPlaceList(onedayPlaceArrayList);
    }

    @Override   //앱바에서 친구추가 기능과 뒤로가기
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.friend_itinerary:
                Toast.makeText(mcontext, "친구추가 클릭됨", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        day = mTablayout.getSelectedTabPosition()+1;
        switch (v.getId()){
            case R.id.plus_floating_itinerary:
                if (onedayPlaceArrayList ==null){
                    next =0;
                    //등록된 일정이 없을 경우
                }else {
                    //등록된 일정이 1개 이상인 경우
                    next =onedayPlaceArrayList.size();
                }
         //       Log.d(TAG, "onClick: name "+onedayPlaceArrayList.get(0).getPlacename());
                Log.d(TAG, "onClick: next "+next);

                Log.d(TAG, "onClick: "+day);
          //      Log.d(TAG, "onClick: arraylist.size()"+onedayPlaceArrayList.size());
                Intent plusintent = new Intent(ItineraryActivity.this, LocationAutocompleteActivity.class);
                plusintent.putExtra("일정", countrycode);
                plusintent.putExtra("언제", day);
                plusintent.putExtra("도시",tnum);
                plusintent.putExtra("다음번호",next);
                startActivity(plusintent);
                break;


        }
    }



}
