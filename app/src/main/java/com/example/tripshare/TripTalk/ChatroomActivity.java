package com.example.tripshare.TripTalk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import com.example.tripshare.Adapter.ChatRoomAdapter;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.Room;
import com.example.tripshare.Data.RoomList;
import com.example.tripshare.LiveStream.StreamingActivity;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.MainActivity;
import com.example.tripshare.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatroomActivity extends AppCompatActivity {

    private static final String TAG = "ChatroomActivity";
    public static ApiInterface apiInterface;
    public static PrefConfig prefConfig;
    private String myemail, Roomnumber, youremail, yourname, yourimgurl;

    private RecyclerView recyclerView;
    private ChatRoomAdapter adapter;
    private Context ctx;
    private LinearLayout linearLayout;
    private ArrayList<Room> roomlist;
    BottomNavigationView bottomNavigationView;
    SimpleDateFormat format = new SimpleDateFormat("yyyy년 M월 d일 E a K시 m분");

    /** Messenger for communicating with the service. */
    Messenger mService = null;
    private final Messenger mMessenger = new Messenger( new comingtomeHandler() );
    /** Flag indicating whether we have called bind on the service. */
    private boolean mBound;
    //최근에 온 메세지의 채팅방을 맨 위로 변경시켜주기
    private boolean positionchange;
    private String rnum;

    class comingtomeHandler extends Handler {

        @Override
        public void handleMessage( Message msg ){
            switch ( msg.what ){
                case TalkService.MSG_SET_RNUM:
                    com.example.tripshare.Data.Message message = (com.example.tripshare.Data.Message) msg.obj;
                    Log.d(TAG, "handleMessage:받은메세지  "+message.getMessage());
                    Log.d(TAG, "handleMessage:받은메세지  "+message.getRnum());
                    Log.d(TAG, "handleMessage:받은메세지  "+message.getSenderemail());
                    rnum =  message.getRnum();
                    positionchange = true;
                    //room list중에서 텍스트 메세지가 온 채팅방(추가나 나간 것은 제외)
                    //가장 최근 메세지랑 시간 수정해주고, 채팅방 리스트 update 해주기
                    if (!message.getMessage().equals("^___join___^")){
                        for (int i = 0; i<roomlist.size() ; i++){
                            Log.d(TAG, "handleMessage: "+i);
                            if (roomlist.get(i).getRnum().equals(message.getRnum())){
                                Log.d(TAG, "handleMessage:rnum "+message.getRnum());
                                roomlist.get(i).setLastmessage(message.getMessage());
                                roomlist.get(i).setHm(message.getHm());
                                roomlist.get(i).setYmd(message.getYmd());
                                break;
                            }
                        }
                        showroomlist(roomlist);
                    }

                    break;
                case TalkService.IMG_SET_RNUM:
                    com.example.tripshare.Data.Message message1 = (com.example.tripshare.Data.Message) msg.obj;
                    Log.d(TAG, "handleMessage:이미지 ");
                    for (int i = 0; i<roomlist.size() ; i++){
                        if (roomlist.get(i).getRnum().equals(message1.getRnum())){
                            roomlist.get(i).setLastmessage("이미지를 전송했습니다.");
                            roomlist.get(i).setHm(message1.getHm());
                            roomlist.get(i).setYmd(message1.getYmd());
                            break;
                        }
                    }
                    showroomlist(roomlist);
                    break;
                default:
                    super.handleMessage( msg );
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            Log.d(TAG, "onServiceConnected: "+className);
            //service에서 받은 IBinsder를 가지고 messenger 객체 생성
            //messenger객체는 service로 데이터를 보낸다. 즉 서비스와 통신한다.
            mService = new Messenger(service);
            mBound = true;
//            sayHello();
            try{
                //보낼 메세지를 생성한다.
                Message msg = Message.obtain( null, TalkService.MSG_REGISTER_CLIENT );
                //서비스가 응답을 보낼 메세지를 받기 위한 messanger객체를 보낼 message에 넣는다.
                msg.replyTo = mMessenger;
                //IBinder를 가진 messenger객체를 통해 메세지를 보낸다.
                mService.send(msg);
                Log.d(TAG, "onServiceConnected: set "+TalkService.MSG_SET_RNUM);

                //사용자 채팅방과 메세지가 온 채팅방을 비교
                String checkroom = "roomlist";
                msg = Message.obtain( null, TalkService.MSG_SET_RNUM, checkroom);
                mService.send(msg);
            }

            catch( RemoteException e ){  }

        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: "+name);
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: bind");
        // Bind to the service
        bindService(new Intent(this, TalkService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            Log.d(TAG, "onStop:unbind ");
            unbindService(mConnection);
            mBound = false;
        }
    }
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        ctx = getApplicationContext();
        prefConfig = new PrefConfig(ctx);
        linearLayout = findViewById(R.id.linear_chatroom);
        recyclerView = findViewById(R.id.recy_chatroom);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ctx);
        recyclerView.setLayoutManager(layoutManager);

        myemail = prefConfig.readEmail();

        //bottom navi view
        bottomNavigationView = findViewById(R.id.bottom_room);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {

            switch (menuItem.getItemId()) {
                case R.id.action_friend:
                    Intent chatintent = new Intent(ChatroomActivity.this, ChatActivity.class);
                    chatintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(chatintent);
                    break;
                case R.id.action_chatroom:
                    break;
                case R.id.action_video:
                    Intent intent = new Intent(ChatroomActivity.this, StreamingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
            }
            return true;
        });
        bottomNavigationView.setSelectedItemId(R.id.action_chatroom);

        //액션바 설정(제목,홈버튼,친구추가버튼)
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("채팅방");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        roomlist = new ArrayList<>();
        //가장 최근에 온 메세지의 채팅방을 맨 위로 하기 위한 변수
        //처음에는 false로 채팅방 번호 비교를 안한다.
        positionchange = false;

        chatroomlist();
    }

    private void chatroomlist() {
    apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
    Call<RoomList> call = apiInterface.myroomlist(myemail);
    call.enqueue(new Callback<RoomList>() {
        @Override
        public void onResponse(Call<RoomList> call, Response<RoomList> response) {
            if (response.isSuccessful()){
                RoomList roomList = response.body();
                Log.d(TAG, "onResponse: "+roomList.getResponse());
                if (roomList.getResponse().equals("success")){

                   showroomlist(roomList.getRoomlist());

                }

            }else {
                Log.d(TAG, "onResponse: error");
            }
        }

        @Override
        public void onFailure(Call<RoomList> call, Throwable t) {
            Log.d(TAG, "onFailure: "+t);
        }
    });

    }

    private void showroomlist(ArrayList<Room> RoomList) {

        roomlist = RoomList;
        ArrayList<Room> roomreversedlist = new ArrayList<>();

        if (RoomList.size() != 0){
            //날짜, 시간을 꺼내서 date형식으로 다시 넣어주기
            //마지막 메세지의 시간별 가장 빠른 순으로 정렬
            for (int n=0 ; n <roomlist.size() ; n++){
                String date = roomlist.get(n).getYmd();
                String time = roomlist.get(n).getHm();
                String plus = date+" "+time;
                Log.d(TAG, "showroomlist: plus "+plus);

                try {
                    Date datetime = format.parse(plus);
                    roomlist.get(n).setDatetime(datetime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                roomlist.sort((o1,o2)  -> {
                    if (o1.getDatetime() ==null || o2.getDatetime() == null)
                        return 0;
                  return o1.getDatetime().compareTo(o2.getDatetime());
                });
            }
            //Collections.sort(roomlist);
        for (int v = roomlist.size()-1 ; v >=0 ; v--) {
            roomreversedlist.add(roomlist.get(v));
            Log.d(TAG, "showroomlist:changed " + roomlist.get(v).getDatetime());
        }

            if (positionchange){
                adapter = new ChatRoomAdapter(ctx, roomreversedlist);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                //처음 채팅방 목록에 온게 아니고
                //가장 최근에 온 메세지가 있을 경우
                //해당 메세지가 온 채팅방을 맨 위로 올린다.
                //즉 list에서 index를 0으로 바꾼다.
                Log.d(TAG, "showroomlist: positionchange");
//                for (int i = 0 ; i<roomlist.size() ;i++ ){
//                    if (roomlist.get(i).getRnum().equals(rnum)){
//                       Room room =  roomlist.get(i);
//                       roomlist.remove(i);
//                       roomlist.add(0,room);
//                        Log.d(TAG, "showroomlist: "+i+"번째 "+rnum+"채팅방을 0번째로 바꾼다." );


//                       break;
//                    }



            }else {
            //처음 채팅방 목록 보여주는 경우
                adapter = new ChatRoomAdapter(ctx, roomreversedlist);
                recyclerView.setAdapter(adapter);
            }


        }
        Log.d(TAG, "showroomlist: room size 0");
    }

    @Override //앱바 만드는 곳
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatroom, menu);

        MenuItem searchitem = menu.findItem(R.id.search_chatroom);
        SearchView searchView = (SearchView) searchitem.getActionView();
        //x버튼
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchitem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                linearLayout.setVisibility(View.GONE);
                bottomNavigationView.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                linearLayout.setVisibility(View.VISIBLE);
                bottomNavigationView.setVisibility(View.VISIBLE);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //엔터나 확인 버튼을 누르면 시작된다.
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            //검색하는 텍스트가 바뀔 때 마다 실행된다.
            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override   //앱바에서 지도, 뒤로가기 클릭할 때
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choosefriend:
                Intent chooseintent = new Intent(ChatroomActivity.this, ChooseFriends.class);
                startActivity(chooseintent);
                return true;
            case android.R.id.home:
                //메인으로
                Intent intent = new Intent(ChatroomActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
