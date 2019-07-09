package com.example.tripshare.Trip;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.MyItem;
import com.example.tripshare.Data.Results;
import com.example.tripshare.Data.TripData;
import com.example.tripshare.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private String tstart,tend,placename;
    private int day, tnum,term;
    private static ApiInterface apiInterface;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    LatLng position;
    View view;
    private TextView marker;
    private ClusterManager<MyItem> myItemClusterManager;
    private ArrayList<Results> resultsArrayList;
    ArrayList<MyItem> myItemslist;
    int marknum;
    MyItem myItem;
    private double latitude,longitude;
    String cityplace;
    private double placelat,placelng;
    private int plusday;
    private String plusedplace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        marknum = 0;

        view = LayoutInflater.from(this).inflate(R.layout.marker, null);
        marker = (TextView) view.findViewById(R.id.loca_marker);

        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        Intent intent = getIntent();
        resultsArrayList =(ArrayList<Results>)intent.getSerializableExtra("resultslist");
        latitude=intent.getDoubleExtra("latitude",0);
        longitude=intent.getDoubleExtra("longitude",0);
        tnum = intent.getIntExtra("tnum",0);
        term = intent.getIntExtra("term",0);
        tstart = intent.getStringExtra("tstart");
        tend = intent.getStringExtra("tend");
        placename = intent.getStringExtra("placename");


        Log.d(TAG, "onCreate: ");
        myItemslist = new ArrayList<>();
        for (Results results : resultsArrayList){
            Log.d(TAG, "onCreate:result "+results.getName());
            myItem = new MyItem(results.getLatitude(),results.getLongitude(), results.getName(),results.getUser_ratings_total());
            myItemslist.add(myItem);
        }

        getmap();

    }


    private void getmap() {

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_mapsactivity);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        setupcluster();
//       getmarker();

        mMap.setOnInfoWindowClickListener(marker -> {
            Log.d(TAG, "onInfoWindowClick: markaer"+marker.getPosition());
            Log.d(TAG, "onInfoWindowClick: "+marker.getTitle());
            cityplace = marker.getTitle();
            placelng =  marker.getPosition().longitude;
            placelat = marker.getPosition().latitude;
            Log.d(TAG, "onMapReady:위경도 "+latitude+"\n"+longitude);

            String tenddate = tend.substring(4);
            Log.d(TAG, "onMapReady:end "+tenddate);
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle(placename+"여행 일정에 추가"+"\n"+tstart+"~"+tenddate);
//            builder.setMessage(tstart+"~"+tenddate);

            Log.d(TAG, "onMapReady:term "+term);
            List<String> daylist = new ArrayList<>();
            for (int i =1 ; i<=term; i++){
                daylist.add(i+"day");
            }

            CharSequence[] items = daylist.toArray(new String[daylist.size()]);
            //선택된 값 한 개 만을 가지고 있는 아이템
            List selecteditem = new ArrayList();
            int defaultitem = 0;
            selecteditem.add(defaultitem);

            builder.setSingleChoiceItems(items, defaultitem,
                    (dialog, which) -> {
                    selecteditem.clear();
                    selecteditem.add(which);
                    });
            builder.setPositiveButton("추가", (dialog, which) -> {

                if (!selecteditem.isEmpty()){
                    int index = (int) selecteditem.get(0);
                    Log.d(TAG, "onClick:index "+index);
           //           daylist.get(index);
                    plusday = index+1;
                    Log.d(TAG, "onClick:plusday "+plusday);

                    Log.d(TAG, "onClick:check "+tnum+"\n"+placelat+"\n"+placelng+"\n"+cityplace+"\n"+plusday+"\n");

                    Call<TripData> call = apiInterface.plusday(tnum,placelat,placelng,cityplace,plusday);
                    call.enqueue(new Callback<TripData>() {
                        @Override
                        public void onResponse(Call<TripData> call, Response<TripData> response) {
                            if (response.isSuccessful()){
                                Log.d(TAG, "onResponse: "+response.body().getResponse());
                                Intent intent = new Intent(MapsActivity.this, ItineraryActivity.class);
                                intent.putExtra("tnum", tnum);
                                intent.putExtra("term",term);
                                intent.putExtra("latitude",latitude);
                                intent.putExtra("longitude",longitude);
                                intent.putExtra("countrycode", "kr");
                                intent.putExtra("tabat", plusday);
                                startActivity(intent);
                            }else {
                                Log.d(TAG, "onResponse: error");
                            }
                        }

                        @Override
                        public void onFailure(Call<TripData> call, Throwable t) {
                            Log.d(TAG, "onFailure: "+t);
                        }
                    });

                }
            }).setNegativeButton("취소", (dialog, which) -> {
                Log.d(TAG, "onMapReady:취소 ");
            });
            builder.show();

        });
    }

    private void setupcluster() {
//        // Set some lat/lng coordinates to start with.
//        double lat = 51.5145160;
//        double lng = -0.1270060;
//
//        // Add ten cluster items in close proximity, for purposes of this example.
//        for (int i = 0; i < 10; i++) {
//            double offset = i / 60d;
//            lat = lat + offset;
//            lng = lng + offset;
//            MyItem offsetItem = new MyItem(lat, lng);
//            ArrayList<MyItem> mPosi = new ArrayList<>();
//            mPosi.add(offsetItem);
//           myItemClusterManager.addItem(offsetItem);
//        }
        // Position the map.
        Log.d(TAG, "setupcluster:lat,mlon "+longitude+"\n"+latitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 6));


        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        myItemClusterManager = new ClusterManager<>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(myItemClusterManager);
        mMap.setOnMarkerClickListener(myItemClusterManager);

        // Add cluster items (markers) to the cluster manager.
        addItems();
    }

    private void addItems() {

//        // Set the lat/long coordinates for the marker.
//        double lati = 51.5009;
//        double longti = -0.122;
//
//        // Set the title and snippet strings.
//        String title = "title";
//        String snippet = "snippet";
//
//        // Create a cluster item(one marker) for the marker and set the title and snippet using the constructor.
//        MyItem infoWindowItem = new MyItem(lati, longti, title, snippet);

        // Add the cluster item (marker) to the cluster manager.

        for (int i = 0 ; i < myItemslist.size() ; i++){
            Log.d(TAG, "addItems:size "+i);
            Log.d(TAG, "addItems:name "+myItemslist.get(i).getTitle());
            myItemClusterManager.addItem(myItemslist.get(i));

        }

    }

    private void getmarker() {

       /* List latlist = new ArrayList();
        List longlist = new ArrayList();
        //장소에 대한 여러 정보를 가지고 있는 리스트를 가져와 위,경도만 갖는 리스트로 바꿔줌
        for (int i =0 ; i< resultsArrayList.size() ; i++){
          //  latlongList.add(new Markerlatlong(locationArrayList.get(i).getLatitude(),locationArrayList.get(i).getLongitude(), locationArrayList.get(i).getPlacename()));
            latlist.add(resultsArrayList.get(i).getLatitude());
            longlist.add(resultsArrayList.get(i).getLongitude());
        }

        Log.d(TAG, "getmarker: latmax= "+ Collections.max(latlist));
        Log.d(TAG, "getmarker: latmin="+Collections.min(latlist));
        Log.d(TAG, "getmarker: longmax="+Collections.max(longlist));
        Log.d(TAG, "getmarker: longmin="+Collections.min(longlist));
        double maxlat = (double) Collections.max(latlist);
        double maxlong = (double) Collections.max(longlist);
        double minlat = (double) Collections.min(latlist);
        double minlong = (double) Collections.min(longlist);
        //최남서쪽과 최동북쪽을 기준으로
        LatLngBounds focus = new LatLngBounds(new LatLng(minlat,minlong), new LatLng(maxlat, maxlong));
*/
        //마커를 추가해줌
        for (Results results : resultsArrayList) {
            marknum = marknum + 1;
            addMarker(results);

        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(resultsArrayList.get(0).getLatitude(), resultsArrayList.get(0).getLongitude()), 18));
        //  mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(focus, 0));
    }


    private Marker addMarker(Results results) {
        //장소 하나당 마커 하나 생성하고 생성할 때 위치랑 이름 지정
        LatLng position = new LatLng(results.getLatitude(), results.getLongitude());

        String rating = String.valueOf(results.getRating());
        //marker.setText(String.valueOf(marknum));
        marker.setText(rating);
        marker.setBackgroundResource(R.drawable.threebluecicle);
        marker.setTextColor(Color.WHITE);
        //마커를 넣어줄 Option 객체
        MarkerOptions markerOptions = new MarkerOptions();
        //마커에서 보여질 이름
        Log.d(TAG, "addMarker:markerlatlong.getName() " + results.getName());
        String name = results.getName();
        markerOptions.title(name);
        //마커의 위치
        markerOptions.position(position);

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, view)));

        return mMap.addMarker(markerOptions);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: ");
        Log.d(TAG, "onMarkerClick: "+marker.getTitle());
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();

        return false;
    }

//    @Override
//    public boolean onMarkerClick(Marker marker) {
//        //사용자가 클릭한 마커의 위경도가 중앙에 오게 카메라를 움직인다.
//        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
//        mMap.animateCamera(center);
//        return true;
//    }


}
