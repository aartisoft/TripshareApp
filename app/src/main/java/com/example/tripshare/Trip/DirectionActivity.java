package com.example.tripshare.Trip;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.tripshare.Direction.DirectionFinder;
import com.example.tripshare.Direction.DirectionFinderListener;
import com.example.tripshare.Direction.Route;
import com.example.tripshare.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectionActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, DirectionFinderListener {
    private TextView orgintxt, destinationtxt, noresulttxt, durationtxt,distancetxt;
    private ImageView mycatimg, transitimg,walkimg,bikeimg;
    private LinearLayout resultimg;
    private GoogleMap mMap;
    private Location mLastKnownLocation;
    //위치 접근 권한을 얻기 위해 필요한 변수
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    //출발,도착마커에 대한 정보,경로에 대한 정보 담을 리스트
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    //길찾기 하는 동안 로딩 알려주는 것
    private ProgressDialog progressDialog;
    private double orlat, orlong, dslat, dslong;
    private String orname, dsname;
    //길찾기 정보 주세요!! 요청할 url과 url에 넣을 내 api
    private static final String TAG = "DirectionActivity";
    //마커에 출발인지,도착인지 표시해 주기 위해
    private int marknum;
    private TextView markertext;
    private View view;
    private String method;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        //textview
        orgintxt = findViewById(R.id.orgin_Tx_direction);destinationtxt= findViewById(R.id.destination_Tx_direction);
        noresulttxt = findViewById(R.id.noresult_tx_Direction); durationtxt = findViewById(R.id.resultduration_txt_Direction);
        distancetxt = findViewById(R.id.resultdistance_txt_Direction);
        //imgview, linearlayout
        mycatimg = findViewById(R.id.mycar_image_direction);transitimg = findViewById(R.id.transit_image_direction);
        walkimg =findViewById(R.id.walk_image_direction);bikeimg = findViewById(R.id.bike_image_direction);
        resultimg = findViewById(R.id.result_linear_direction);
        mycatimg.setOnClickListener(this); transitimg.setOnClickListener(this); walkimg.setOnClickListener(this);bikeimg.setOnClickListener(this);
        orlat = getIntent().getDoubleExtra("orlat",0);
        orlong = getIntent().getDoubleExtra("orlong",0);
        dslat = getIntent().getDoubleExtra("dslat",0);
        dslong = getIntent().getDoubleExtra("dslong",0);

        //내가 만든 마커, 장소 순서를 사용자에게 알려주기 위해
        view = LayoutInflater.from(this).inflate(R.layout.marker,null);
        markertext = (TextView)view.findViewById(R.id.loca_marker);

        orname= getIntent().getStringExtra("orname");
        dsname = getIntent().getStringExtra("dsname");
        Log.d(TAG, "onCreate: orname "+ orname);
        //출발,도착지를 입력해줌줌
        orgintxt.setText(orname);
        destinationtxt.setText(dsname);

        durationtxt.setText(orname);
        distancetxt.setText(dsname);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_direction);
        mapFragment.getMapAsync(this);
        method = "mycar";
        mycatimg.setBackgroundColor(R.color.colorPrimary);
        sendRequest(method);
    }

    private void sendRequest(String way) {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);
        method = way;
        try {
            //출발지와 목적지를 가지고 구글에게 길찾아 달라고 요청한다.
            Log.d(TAG, "sendRequest:or,ds "+orlat+"\n"+dslat);
                new DirectionFinder(this, orlat, orlong,dslat,dslong,way).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add paging marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocationPermission();
    }
    //사용자 기기 위치 접근 권한이 있는지 확인해서 없으면 권한 요청을 한다.
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: ");
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by paging callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Log.d(TAG, "getLocationPermission: 위치 접근 권한 이미 있어서 요청 다시 안해");
            updateLocationUI();
        } else {
            Log.d(TAG, "getLocationPermission: 위치 권한 없어서 요청합니다.");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    //권한 요청 결과
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: ");
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
        updateLocationUI();
    }
    //권한이 있으므로 GPS버튼을 넣어주어 내 위치를 찾을 수 있게 한다.
    private void updateLocationUI() {
        if (mMap == null) {
            Log.d(TAG, "updateLocationUI:null ");
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                Log.d(TAG, "updateLocationUI:granted ");
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                Log.d(TAG, "updateLocationUI:not granted ");
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.d(TAG, "updateLocationUI:error ");
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.mycar_image_direction :
                method = "driving";
                mycatimg.setBackgroundColor(R.color.colorPrimary);
                transitimg.setBackgroundColor(Color.WHITE);
                walkimg.setBackgroundColor(Color.WHITE);
                bikeimg.setBackgroundColor(Color.WHITE);
                sendRequest(method);
                break;

            case  R.id.transit_image_direction :
                method = "transit";
                transitimg.setBackgroundColor(R.color.colorPrimary);
                mycatimg.setBackgroundColor(Color.WHITE);
                walkimg.setBackgroundColor(Color.WHITE);
                bikeimg.setBackgroundColor(Color.WHITE);

                sendRequest(method);
            break;

            case R.id.walk_image_direction :
                method ="walking";
                walkimg.setBackgroundColor(R.color.colorPrimary);
                transitimg.setBackgroundColor(Color.WHITE);
                mycatimg.setBackgroundColor(Color.WHITE);
                bikeimg.setBackgroundColor(Color.WHITE);
                sendRequest(method);
                Log.d(TAG, "onClick:walk ");
                break;

            case R.id.bike_image_direction :
                method = "bicycling";
                Log.d(TAG, "onClick:bike ");
                transitimg.setBackgroundColor(Color.WHITE);
                walkimg.setBackgroundColor(Color.WHITE);
                bikeimg.setBackgroundColor(Color.WHITE);
                bikeimg.setBackgroundColor(R.color.colorPrimary);
                sendRequest(method);
                break;

        }
    }

    @Override
    public void onDirectionFinderStart() {

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        resultimg.setVisibility(View.VISIBLE);
        noresulttxt.setVisibility(View.INVISIBLE);

        ArrayList latlist = new ArrayList<>();
        latlist.add(0,orlat);
        latlist.add(1,dslat);
        ArrayList longlist = new ArrayList<>();
        longlist.add(0,orlong);
        longlist.add(1,dslong);

        double maxlat = (double) Collections.max(latlist);
        double maxlong = (double) Collections.max(longlist);
        double minlat = (double) Collections.min(latlist);
        double minlong = (double) Collections.min(longlist);

        Log.d(TAG, "onDirectionFinderSuccess:max ");
        LatLngBounds focus = new LatLngBounds(new LatLng(minlat,minlong), new LatLng(maxlat,maxlong));


        for (Route route : routes) {
            //시간 거리를 입력해줌
            ((TextView) findViewById(R.id.resultduration_txt_Direction)).setText("시간 : "+route.duration.text);
            ((TextView) findViewById(R.id.resultdistance_txt_Direction)).setText("거리 : "+route.distance.text);

            //marker에 들어갈 텍스트,배경,색깔 정하고 xml레이아웃을 비트맵으로 만듬
            Log.d(TAG, "onDirectionFinderSuccess: ");
            markertext.setText("출발");
            markertext.setBackgroundResource(R.drawable.threebluecicle);
            markertext.setTextColor(Color.WHITE);
            Bitmap custommarker =createDrawableFromView(this,view);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title(orname).position(route.startLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(custommarker));
            originMarkers.add(mMap.addMarker(markerOptions));


            /*originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.threebluecicle))
                    .title(route.startAddress)
                    .position(route.startLocation)));
*/

            //marker에 들어갈 텍스트,배경,색깔 정하고 xml레이아웃을 비트맵으로 만듬
            markertext.setText("도착");
            markertext.setBackgroundResource(R.drawable.threebluecicle);
            markertext.setTextColor(Color.WHITE);
            Bitmap dsmarker =createDrawableFromView(this,view);

            MarkerOptions dsmarkerOptions = new MarkerOptions();
            dsmarkerOptions.title(dsname).position(route.endLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(dsmarker));
            destinationMarkers.add(mMap.addMarker(dsmarkerOptions));

            /*destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.threebluecicle))
                    .title(route.endAddress)
                    .position(route.endLocation)));*/

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++){
                Log.d(TAG, "onDirectionFinderSuccess: i : "+i);
                polylineOptions.add(route.points.get(i));

            }
            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(focus, 100));
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
    public void onDirectionFinderFail() {
        progressDialog.dismiss();
        //지도랑 결과 안보이게 한다. 반면 결과 없다는 텍스트 보여주기
        resultimg.setVisibility(View.INVISIBLE);
        noresulttxt.setVisibility(View.VISIBLE);
    }
}
