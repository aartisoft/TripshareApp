/**
 * This is sample code provided by Wowza Media Systems, LLC.  All sample code is intended to be a reference for the
 * purpose of educating developers, and is not intended to be used in any production environment.
 * <p>
 * IN NO EVENT SHALL WOWZA MEDIA SYSTEMS, LLC BE LIABLE TO YOU OR ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
 * OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION,
 * EVEN IF WOWZA MEDIA SYSTEMS, LLC HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * WOWZA MEDIA SYSTEMS, LLC SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. ALL CODE PROVIDED HEREUNDER IS PROVIDED "AS IS".
 * WOWZA MEDIA SYSTEMS, LLC HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * <p>
 * © 2015 – 2019 Wowza Media Systems, LLC. All rights reserved.
 */

package com.example.tripshare.LiveStream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.MainActivity;
import com.example.tripshare.R;
import com.example.tripshare.TripTalk.ChatActivity;
import com.example.tripshare.TripTalk.ChatroomActivity;
import com.example.tripshare.TripTalk.OnAirActivity;
import com.example.tripshare.TripTalk.RecyclerViewClickListener;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreamingActivity extends AppCompatActivity implements RecyclerViewClickListener,
        SwipeRefreshLayout.OnRefreshListener {
    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    StreamingAdapter adapter;
    ArrayList<Livestream> livestreamArrayList;
    private static final String TAG = "StreamingActivity";
    Context mtx;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean notify = false;

    public static ApiInterface apiInterface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_streaming);

        mtx = getApplicationContext();

        swipeRefreshLayout = findViewById(R.id.uprefresh_stream);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        bottomNavigationView = findViewById(R.id.bottom_stream);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {

            switch (menuItem.getItemId()) {
                case R.id.action_friend:
                    Intent chatintent = new Intent(StreamingActivity.this, ChatActivity.class);
                    chatintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(chatintent);
                    break;
                case R.id.action_chatroom:
                    Intent roomintent = new Intent(StreamingActivity.this, ChatroomActivity.class);
                    roomintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(roomintent);
                    break;
                case R.id.action_video:
                    break;
            }
            return true;
        });
        bottomNavigationView.setSelectedItemId(R.id.action_video);

        /*adapter = new StreamingAdapter(this, )
        recyclerView.setAdapter(adapter);
        */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("라이브 스트리밍");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerView = findViewById(R.id.recy_streaming);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(StreamingActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        //스트리밍 방 리스트 가져오기
        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(true);

        }
        );
       givestreamlist();
    }


    private void givestreamlist() {
        if (getIntent().getStringExtra("method") !=null){
            try {
                Log.d(TAG, "givestreamlist:2초 ");
                Thread.sleep(2000);
                Log.d(TAG, "givestreamlist:2초 기다린다. ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Call<StreamRoomList> call = apiInterface.giveroomlist();
        call.enqueue(new Callback<StreamRoomList>() {
            @Override
            public void onResponse(Call<StreamRoomList> call, Response<StreamRoomList> response) {
                //요청 결과에 상관없이 스와이프 종료시키기
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful()){
                    StreamRoomList streamRoomList = response.body();


                    Log.d(TAG, "onResponse:roomlist "+streamRoomList.getResponse());
                    //swipeRefreshLayout.setRefreshing(false);
                    //방송 정보를 보여주기
                    if (streamRoomList.getStreamRoomlist() !=null){
                        if (streamRoomList.getStreamRoomlist().size() !=0){
                            showroomlist(streamRoomList.getStreamRoomlist());
                        }
                    }

                }
            }
            @Override
            public void onFailure(Call<StreamRoomList> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t);
                //요청 결과에 상관없이 스와이프 종료시키기
                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    private void showroomlist(ArrayList<Livestream> streamRoomlist) {
        livestreamArrayList = new ArrayList<>();
        livestreamArrayList = streamRoomlist;
        adapter = new StreamingAdapter(mtx, livestreamArrayList, this);
        recyclerView.setAdapter(adapter);
        if (notify){
            Log.d(TAG, "showroomlist:notify ");
            //한번 이상이라면
            adapter.notifyDataSetChanged();
        }
    }

    @Override //앱바 만드는 곳
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.streamplus, menu);
        return true;
    }

    @Override   //앱바에서 지도, 뒤로가기 클릭할 때
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.plusstream:

                /*// Start streaming
                //다이어로그에 들어갈 입력창
                final EditText editText = new EditText(this);

                //방 이름을 정하고 방송을 시작한다.
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(StreamingActivity.this);
                alertDialog.setTitle("방송 제목");
                alertDialog.setMessage("방송 제목을 입력해주세요.");
                alertDialog.setView(editText);
                alertDialog.setPositiveButton("방송 시작", (dialog, which) -> {
                    createlivestream(editText.getText().toString());
                });
                alertDialog.setNegativeButton("취소", (dialog, which) -> {

                });
                alertDialog.show();*/
                createlivestream();
                return true;
            case android.R.id.home:
                //메인으로
                Intent intent = new Intent(StreamingActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createlivestream() {

        Intent startintent = new Intent(StreamingActivity.this, OnAirActivity.class);
        startActivity(startintent);
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        Intent intent = new Intent(StreamingActivity.this, PlayerActivity.class);
        intent.putExtra("room", livestreamArrayList.get(position).getSourceConnectionInformation());
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        //스와이프를 밑으로 하면 호출됨
        //데이터를 달라고 하면 됨
        notify = true;
        givestreamlist();
        //swipeRefreshLayout.setRefreshing(false);
    }
}
