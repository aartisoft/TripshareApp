package com.example.tripshare.TripTalk;

import android.Manifest;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.tripshare.Adapter.MessageAdapter;
import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.Message;
import com.example.tripshare.Data.Messagelist;
import com.example.tripshare.Data.Room;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity implements RecyclerViewClickListener {
    Button submitbt, imagebt;
    EditText messageedit;
    private RecyclerView recyclerView;

    private static final String TAG = "MessageActivity";
    private static final int PhotoPicker = 1006;
    private static final int GALLERY_PICK = 1004;
    private static final int CAMERA_PICK = 1005;
    private static final int READ_EXSTORAGE_REQUEST = 24;
    private static final int CAMERA_REQUEST = 25;


    String message, receiveremail, myname;
    String total;
    public static String myemail, receiver, myurl;

    public static PrefConfig prefConfig;
    public static ApiInterface apiInterface;

    private Context ctx;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messageList;
    String firstmessage;
    Handler handler;
    private String rnum;
    String ymd;
    String hm;
    private boolean mBound;
    int inputtotal;
    private ActionBar actionBar;

    private Bitmap bitmap;
    private String base64str;
    private String type, imgurl;
    //최근에 온 메세지의 채팅방을 맨 위로 변경시켜주기
    Date date1, date2;
    String time1, time2;
    SimpleDateFormat f1 = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
    long now;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    int clickposition;


    /**
     * Messenger for communicating with the service.
     */
    Messenger mService = null;
    private Messenger mMessenger;
    private RecyclerView.LayoutManager layoutManager;
    byte[] imgByte;

    private LinearLayout galinear, calinear, viorgonelinear;
    boolean visibleornot;
    ImageView imgview;
    String currentPhotoPath;
    private ArrayList<Message> imgurllist;
    private ArrayList<String> realpathlist;
    ArrayList<Uri> imglist;
    ArrayList<byte[]> bytelist;
    int imgposition;
    InputMethodManager imm;

    @Override
    public void recyclerViewListClicked(View v, int position) {
        //  messageAdapter = new MessageAdapter(ctx, this);


        ConstraintLayout constraintLayout = (ConstraintLayout) v;
        int count = constraintLayout.getChildCount();
        Log.d(TAG, "recyclerViewListClicked:count " + count);
        for (int i = 0; i < count; i++) {
            View view = constraintLayout.getChildAt(i);
            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                Log.d(TAG, "recyclerViewListClicked:이미지 보이는 속성 " + imageView.getVisibility());

                switch (imageView.getId()) {
                    case R.id.img_sender:
                        //내가 보낸 이미지가 보이면 8, 안 보이면 0
                        if (imageView.getVisibility() == View.VISIBLE) {
                            Log.d(TAG, "recyclerViewListClicked: 내가 보낸 이미지 클릭함");
                            getclickimgposition(position);
                            Log.d(TAG, "recyclerViewListClicked: clickposition " + clickposition);
                            //position 가지고 picture 확인 액티비티로 이동
                            gotopictureA();
                        } else {
                            Log.d(TAG, "recyclerViewListClicked:내가 보낸 텍스트 클릭함 ");
                        }
                        Log.d(TAG, "recyclerViewListClicked:sender ");
                        break;
                    case R.id.img_receiver:
                        //상대방이 보낸 이미지가 보이면 8, 안 보이면 0
                        if (imageView.getVisibility() == View.VISIBLE) {
                            Log.d(TAG, "recyclerViewListClicked:니가 보낸 이미지 클릭 not 프로필 이미지");
                            //position 가지고 picture 확인 액티비티로 이동
                            getclickimgposition(position);
                            Log.d(TAG, "recyclerViewListClicked: clickposition " + clickposition);
                            gotopictureA();
                        } else {
                            Log.d(TAG, "recyclerViewListClicked:니가 보낸 것중 이미지 말고 다른것들  ");
                        }
                        Log.d(TAG, "recyclerViewListClicked:receiver ");
                        break;
                }
            }
        }
    }

    private void gotopictureA() {
        Intent intent = new Intent(MessageActivity.this, PictureActivity.class);
        intent.putExtra("rnum", rnum);
        intent.putExtra("clickposition", clickposition);
        startActivity(intent);
    }

    private void getclickimgposition(int position) {
        //이미지만을 담을 메세지 리스트
        ArrayList<Message> melist = new ArrayList<>();

        Log.d(TAG, "recyclerViewListClicked:메세지의 총 개수 " + messageList.size());
        for (int posi = 0; posi < messageList.size(); posi++) {
            //새로 만든 이미지 리스트
            if (messageList.get(posi).getType().equals("image")) {
                melist.add(messageList.get(posi));
            }
        }
        //사용자가 선택한 이미지가 새로 만든 이미지 리스트의 어느 index에 위치하는지
        Log.d(TAG, "recyclerViewListClicked:총 이미지 개수 " + melist.size());
        for (int compare = 0; compare < melist.size(); compare++) {
            //사용자가 클릭한 이미지와
            //1. url이 존재하고(!= "no") 같은지
            //2. url이 존재하지 않는다면(== "no")
            //비트맵 문자열이 같다면 해당 compare값이 인텐트로 넘겨줄 clickposition 값이다.
            Log.d(TAG, "recyclerViewListClicked:클릭한 이미지 url " + messageList.get(position).getMessage());
            if (!messageList.get(position).getMessage().equals("no")) {
                //사용자가 클릭한 이미지 url이 존재하는 경우
                if (messageList.get(position).getMessage().equals(melist.get(compare).getMessage())) {
                    //imgurl이 클릭한 것과 같은 경우
                    clickposition = compare;
                    Log.d(TAG, "recyclerViewListClicked:url and equal " + compare);
                    break;
                } else {
                    Log.d(TAG, "recyclerViewListClicked:이 이미지는 클릭한 이미지가 아니야 " + compare);
                }
            } else {

                Log.d(TAG, "recyclerViewListClicked:Is imgurl no? " + messageList.get(position).getMessage());
                if (messageList.get(position).getBitmaptoString().equals(melist.get(compare).getBitmaptoString())) {
                    //클릭 이미지가 url이 없고 비트맵 문자열이 있는 경우
                    clickposition = compare;
                    Log.d(TAG, "recyclerViewListClicked: " + compare);
                    break;
                } else {
                    Log.d(TAG, "recyclerViewListClicked:이 이미지는 클릭한 이미지가 아니야 " + compare);

                }
            }
        }
    }

    /**
     * Flag indicating whether we have called bind on the service.
     */

    class comingtomeHandler extends Handler {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TalkService.MSG_SET_RNUM:
                    com.example.tripshare.Data.Message message = (com.example.tripshare.Data.Message) msg.obj;
                    Log.d(TAG, "handleMessage:service에서 받은메세지  " + message.getMessage());
                    Log.d(TAG, "handleMessage:service에서 받은채팅방  " + message.getRnum());
                    Log.d(TAG, "handleMessage:service에서 받은 보낸사람 이메일  " + message.getSenderemail());
                    //메세지를 받았으나 더 필요한 것은 보낸 사람의 url과 이름이다.
                    //지금은 쉽게 가능한 데 다중채팅때 url을 어떻게 가져올까 고민이 들었다.
                    //이것은 나중에 채팅방에 들어올 때 사용자 정보를 arraylist로 보내는 방법을 생각하고 있다.
                    //거기서 사용자 email에 해당하는 사람의 url과 이름을 가져오면 되겠다.
                    if (messageList == null) {
                        messageList = new ArrayList<>();
                    }

                    Log.d(TAG, "handleMessage: backimg" + message.getImgurl() + "\n" + myurl);

                    if (message.getMessage().equals("^___goout___^")) {

                        //채팅방 구성원 중 한 명이 나갔을 경우
                        //1. 앱바 수정
                        Log.d(TAG, "handleMessage: total" + message.getTotal());
                        total = message.getTotal();
                        if (total.equals("1")) {
                            actionBar.setTitle("1:1 채팅");
                        } else if (total.equals("0")) {
                            String title = "알 수 없음";
                            actionBar.setTitle(title);
                        } else {
                            String title = "그룹 채팅 " + total;
                            actionBar.setTitle(title);
                        }

                        if (!total.equals("0")){
                            //채팅방에 상대방이 없다면
                            //2. 메세지 보낼 사람 수정하기
                            String outemail = message.getSenderemail();
                            Log.d(TAG, "handleMessage: outemail " + outemail);
                            String[] exemails = receiveremail.split(",");
                            String newreceiveremail = "";
                            for (int ex = 0; ex < exemails.length; ex++) {
                                if (!outemail.equals(exemails[ex])) {
                                    //기존 받는 사람 이메일에서 나간 사람 이메일을 제거해준다.
                                    newreceiveremail = exemails[ex] + "," + newreceiveremail;
                                }
                            }
                            Log.d(TAG, "handleMessage: newreceiver " + newreceiveremail);
                            receiveremail = newreceiveremail.substring(0, newreceiveremail.length() - 1);
                            Log.d(TAG, "messagetoChatroomA: receiveremail " + receiveremail);
                        }


                        //나갔다는 메세지 추가하기
                        messageList.add(message);
                        messagein(messageList);

                    } else if (message.getMessage().equals("^___join___^")) {
                        //초대 메세지인 경우 채팅방 이름 수정해줌
                        actionBar.setTitle("그룹 채팅 " + message.getTotal());
                        //메세지를 받을 사람들에 새로 추가된 사람들 넣어 줌
                        Log.d(TAG, "handleMessage: send for emails " + message.getSenderemail());
                        receiveremail = receiveremail + "," + message.getSenderemail();
                        //메세지 받을 사람 수도 변경해 줌
                        total = String.valueOf(Integer.valueOf(message.getTotal()) - 1);
                        if (!message.getImgurl().equals(myurl)) {
                            //내가 초대한 사람이 아니면 메세지에 추가해줌

                            messageList.add(message);
                            messagein(messageList);
                        }
                    } else {
                        //초대 메세지가 아닌경우
                        messageList.add(message);
                        messagein(messageList);
                    }

                    break;
                case TalkService.IMG_SET_RNUM:
                    com.example.tripshare.Data.Message message1 = (com.example.tripshare.Data.Message) msg.obj;
                    Log.d(TAG, "handleMessage:image ");

                    //받은 이미지 서버에 저장
                    //saveimage();

                    //내 핸드폰에 보여주기
                    if (messageList == null) {
                        messageList = new ArrayList<>();
                    }
                    //초대 메세지인 경우 채팅방 이름 수정해줌
                    Log.d(TAG, "handleMessage: backimg" + message1.getImgurl() + "\n" + myurl);

                    //초대 메세지가 아닌경우
                    messageList.add(message1);
                    messagein(messageList);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {


            mMessenger = new Messenger(new comingtomeHandler());
            Log.d(TAG, "onServiceConnected: " + className);
            //service에서 받은 IBinsder를 가지고 messenger 객체 생성
            //messenger객체는 service로 데이터를 보낸다. 즉 서비스와 통신한다.
            mService = new Messenger(service);
            mBound = true;
            Log.d(TAG, "onServiceConnected:bound " + mBound);
            try {
                //보낼 메세지를 생성한다.
                android.os.Message msg = android.os.Message.obtain(null, TalkService.MSG_REGISTER_CLIENT);
                //서비스가 보낸 메세지를 받기 위한 messanger객체를 보낼 message에 넣는다.
                msg.replyTo = mMessenger;
                //IBinder를 가진 messenger객체를 통해 메세지를 보낸다.
                mService.send(msg);

                //방 번호를 서비스에게 보낸다. 나중에 메세지가 왔을 때 메세지의 방번호가 일치하면 메세지를 준다.
                Log.d(TAG, "onServiceConnected: set " + TalkService.MSG_SET_RNUM);
                Log.d(TAG, "onServiceConnected: ruum " + rnum);
                msg = android.os.Message.obtain(null, TalkService.MSG_SET_RNUM, rnum);
                mService.send(msg);

                //채팅방 이메일을 서비스에게 준다. 나중에 해당 이메일 사용자들에게 메세지를 보낼 수 있게
                msg = android.os.Message.obtain(null, TalkService.MSG_SET_RECEIVERS_EMAIL, receiveremail);
                mService.send(msg);

            } catch (RemoteException e) {
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: " + name);
            mService = null;
            mBound = false;
            Log.d(TAG, "onServiceDisconnected:bound " + mBound);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


        //메세지를 받았을 때 리사이 클러뷰에 넣어주기 위해 사용한다.
        handler = new Handler();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        imgview = findViewById(R.id.img_message);
        message = "message";
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
       /* permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MessageActivity.this, "권한 승인됨  ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MessageActivity.this, "권한이 승인이 안됬어", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("권한이 거부되었다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
*/
        messageList = new ArrayList<>();
        firstmessage = "first";
        ctx = getApplicationContext();
        submitbt = findViewById(R.id.submit_message);
        messageedit = findViewById(R.id.input_message);
        recyclerView = findViewById(R.id.recy_message);
        imagebt = findViewById(R.id.img_bt_message);

        layoutManager = new LinearLayoutManager(ctx);
//       ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        viorgonelinear = findViewById(R.id.vi_or_gone_li_mess);
        calinear = findViewById(R.id.camera_linear_message);
        galinear = findViewById(R.id.gallery_linear_message);

        //보내는 사람
        prefConfig = new PrefConfig(this);
        myemail = prefConfig.readEmail();
        myname = prefConfig.getName();
        myurl = prefConfig.readimgurl();
        Log.d(TAG, "onCreate:myurl " + myurl);
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Log.d(TAG, "onCreate: ");

        //메세지 받을 객체
        messageList = new ArrayList<>();

        visibleornot = false;
        //추가 버튼 클릭했을 때
        imagebt.setOnClickListener(v -> {

            if (visibleornot) {
                imm.showSoftInput(messageedit, 0);
                //갤러리,카메라 이미지 안 보이게
                viorgonelinear.setVisibility(View.GONE);
                //선택 버튼을 취소 버튼으로 바꾼다.
                imagebt.setBackgroundResource(R.drawable.add_message);
                //클릭을 했으니 된 상태라 표시
                visibleornot = false;


            } else {
                imm.hideSoftInputFromWindow(messageedit.getWindowToken(), 0);
                //갤러리,카메라 이미지 보이게 하고
                viorgonelinear.setVisibility(View.VISIBLE);
                //선택 버튼을 취소 버튼으로 바꾼다.
                imagebt.setBackgroundResource(R.drawable.ic_close_black_24dp);
                //클릭을 했으니 된 상태라 표시
                visibleornot = true;

            }

        });

        //갤러리 레이아웃 클릭했을 때
        galinear.setOnClickListener(v -> {

            //외부 저장소 읽고 쓰는 권한 체크와 요청
            if (ContextCompat.checkSelfPermission(MessageActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                Intent galleryin = new Intent();
                galleryin.setType("image/*"); //이미지 파일만을 가져오겠다.
                galleryin.setAction(Intent.ACTION_GET_CONTENT); //앨범에서 가져오겠다.
                galleryin.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                //사용자에게 이미지를 가져오기 위해 어떤 앱을 사용할 것인지 선택을 하게 한다.
                startActivityForResult(galleryin.createChooser(galleryin, "SELECT IMAGE"), GALLERY_PICK);

            } else {
                ActivityCompat.requestPermissions(MessageActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXSTORAGE_REQUEST);


               /* Intent intent = new Intent(this, GalleryActivity.class);
                Params params = new Params();
                params.setPickerLimit(10);
                params.setToolbarColor(R.color.white);
                params.setActionButtonColor(R.color.white);
                params.setButtonTextColor(R.color.darkgray);
                intent.putExtra(Constants.KEY_PARAMS, params);
                startActivityForResult(intent, Constants.TYPE_MULTI_PICKER);
*/
                /*// start multiple photos selector
                Intent intent = new Intent(MessageActivity.this, ImagesSelectorActivity.class);
                // max number of images to be selected
                intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, 5);
                // min size of image which will be shown; to filter tiny images (mainly icons)
                intent.putExtra(SelectorSettings.SELECTOR_MIN_IMAGE_SIZE, 100000);
                 // pass current selected images as the initial value
                intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, mResults);
                // start the selector
                intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, false);

                startActivityForResult(intent, PhotoPicker);*/
               /* TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(MessageActivity.this)
                        .setImageProvider(new TedBottomPicker.ImageProvider() {
                            @Override
                            public void onProvideImage(ImageView imageView, Uri imageUri) {


                            }
                        }).setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(Uri uri) {

                            }
                        })
                        .create();

                tedBottomPicker.show(getSupportFragmentManager());*/
            }

        });


        //카메라 레이아웃을 클릭했을 때
        calinear.setOnClickListener(v -> {
            //카메라 권한이 부여되있는지 확인한 이후에 권한 요청
            if (ContextCompat.checkSelfPermission(MessageActivity.this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(MessageActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate:둘다 권한이 승락되었는데 0이면 권한이 승락됨 -1이면 거부됨 " + ContextCompat.checkSelfPermission(MessageActivity.this,
                        Manifest.permission.CAMERA));
                gotocamera();
            } else {
                Log.d(TAG, "onCreate:0둘 중 하나만 권한이 승라고딤이면 권한이 승락됨 -1이면 거부됨 " + ContextCompat.checkSelfPermission(MessageActivity.this,
                        Manifest.permission.CAMERA));

                ActivityCompat.requestPermissions(MessageActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA}, CAMERA_REQUEST);

            }
        });

        //서버와 연결하고 메세지 받는 스레드 시작
//        connect.start();

        //메세지를 디비에 저장한다.
        //채팅방이 없다면 채팅방도 만든다.
        //채팅방을 만들 경우 채팅방 번호는 채팅방 번호중 가장 큰 번호 +1로 한다.
        //채팅방이 있다면 메세지만 저장한다.
        //받아오는 것은 채팅방 번호이다.
        //채팅방 번호는 1:1 뿐만 아니라
        //단체 채팅할 때 채팅방 번호로 방을 구분할 수 있다.
        submitbt.setOnClickListener(v -> {

            //날짜와 시간을 얻음
            getymdhm();

            Log.d(TAG, "onCreate: email" + myemail + "\n" + receiveremail);

            if (!messageedit.getText().toString().equals("")) {
                message = messageedit.getText().toString();
                messageedit.setText("");
                gettimediffer();
                //이미지가 아닌 텍스트를 보낸다고 알려주기
                type = "mtext";
                if (total.equals("1")) {
                    Log.d(TAG, "text send click myemail " + myemail);
                    Log.d(TAG, "text send click receiver " + receiveremail);
                    Log.d(TAG, "text send click message " + message);
                    Log.d(TAG, "text send click ymd " + ymd);
                    Log.d(TAG, "text send click hm " + hm);

                    //1:1 채팅일 경우
                    //받는 사람에게 메세지를 보냄
                    //서버에 메세지 저장
                    Call<Message> call = apiInterface.messageandroom(myemail, receiveremail, message, ymd, hm);
                    call.enqueue(new Callback<Message>() {
                        @Override
                        public void onResponse(Call<Message> call, Response<Message> response) {
                            if (response.isSuccessful()) {
                                Message savedmessage = response.body();
                                Log.d(TAG, "onResponse:response " + savedmessage.getResponse().equals("success"));
                                if (savedmessage.getResponse().equals("success")) {
                                    Log.d(TAG, "onResponse: 서버에 1:1메세지 저장됨");
                                    gettimediffer2();

                                    //메세지를 보낸 경우 내 화면에 바로 보여줌
                                    com.example.tripshare.Data.Message message1 = new com.example.tripshare.Data.Message(
                                            type, receiveremail, myurl, message, rnum, myname, ymd, hm);

                                    if (messageList == null) {
                                        messageList = new ArrayList<>();
                                    }

                                    messageList.add(message1);
                                    messagein(messageList);

                                    new Sender().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    Toast.makeText(MessageActivity.this, "메세지가 서버에 저장이 안됬습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d(TAG, "onResponse: error");
                            }
                        }

                        @Override
                        public void onFailure(Call<Message> call, Throwable t) {
                            Log.d(TAG, "onFailure: " + t);
                        }
                    });
                } else if (total.equals("0")) {
                    //다른 사람들이 채팅방을 나갔을 경우
                    Toast.makeText(ctx, "메세지를 보낼 수 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    //받는 사람에게 메세지를 보냄


                    type = "mtext";
                    Log.d(TAG, "text multi send click myemail " + myemail);
                    Log.d(TAG, "text multi send click myemail " + myname);
                    Log.d(TAG, "text multi send click myemail " + myurl);
                    Log.d(TAG, "text send click receiver " + receiveremail);
                    Log.d(TAG, "text send click message " + message);
                    Log.d(TAG, "text send click ymd " + ymd);
                    Log.d(TAG, "text send click hm " + hm);

                    //다중채팅일 경우
                    Call<Message> call = apiInterface.multiroommessage(myemail, rnum, message, ymd, hm);
                    call.enqueue(new Callback<Message>() {
                        @Override
                        public void onResponse(Call<Message> call, Response<Message> response) {
                            if (response.isSuccessful()) {
                                Message savedmessage = response.body();
                                if (savedmessage.getResponse().equals("success")) {
                                    Log.d(TAG, "onResponse: 다중 채팅 메세지 저장됨");
                                    gettimediffer2();

                                    //메세지를 보낸 경우 내 화면에 바로 보여줌
                                    com.example.tripshare.Data.Message message1 = new com.example.tripshare.Data.Message(
                                            type, receiveremail, myurl, message, rnum, myname, ymd, hm);
                                    if (messageList == null) {
                                        messageList = new ArrayList<>();
                                    }

                                    messageList.add(message1);
                                    messagein(messageList);

                                    new Sender().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    Toast.makeText(MessageActivity.this, "메세지가 서버에 저장이 안됬습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d(TAG, "onResponse: error");
                            }
                        }

                        @Override
                        public void onFailure(Call<Message> call, Throwable t) {
                            Log.d(TAG, "onFailure: " + t);
                        }
                    });
                }
            }
        });

        //채팅방 리스트에서 채팅방  클릭해서 온 경우 rnum을 가져온다.
        //하지만 유저 프로필에서  경우엔 rnum을 가져오지 못한다.
        if (getIntent().getStringExtra("rnum") != null) {
            rnum = getIntent().getStringExtra("rnum");
            //받는 사람
            receiveremail = getIntent().getStringExtra("email");
            receiver = getIntent().getStringExtra("name");
            Log.d(TAG, "onResume: " + receiveremail);
            total = getIntent().getStringExtra("total");
            Log.d(TAG, "onResume: " + rnum);
            Log.d(TAG, "onResume: " + receiver);
            Log.d(TAG, "onResume: " + total);
            // 채팅 내역 리스트 로컬에서 불러오기
            //inmylocalmessage();
            //서버에서 불러오기
            mychatlist();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                gotocamera();

                Log.d(TAG, "onRequestPermissionsResult:카메라 권환 획득 ");
                break;
            case READ_EXSTORAGE_REQUEST:
                Intent galleryin = new Intent();
                galleryin.setType("image/*"); //이미지 파일만을 가져오겠다.
                galleryin.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                galleryin.setAction(Intent.ACTION_GET_CONTENT); //앨범에서 가져오겠다.
                //사용자에게 이미지를 가져오기 위해 어떤 앱을 사용할 것인지 선택을 하게 한다.
                startActivityForResult(galleryin.createChooser(galleryin, "SELECT IMAGE"), GALLERY_PICK);

                Log.d(TAG, "onRequestPermissionsResult:외부 저장소 읽는 권한 획득 ");
        }
    }

    private void gotocamera() {
        Intent cameintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //인텐트가 갈 액티비티(카메라)가 있다면
        if (cameintent.resolveActivity(getPackageManager()) != null) {
            //사진을 저장할 파일을 만듬
            File photofile = null;
            try {
                photofile = createImageFile();
            } catch (IOException e) {
                Log.d(TAG, "gotocamera:createfile에서 에러 " + e.getMessage());
                e.printStackTrace();
            }
            //파일이 성공적으로 만들어 진면
            if (photofile != null) {
                Uri photouri = FileProvider.getUriForFile(this,
                        "com.example.tripshare.fileprovider",
                        photofile);
                cameintent.putExtra(MediaStore.EXTRA_OUTPUT, photouri);
                startActivityForResult(cameintent, CAMERA_PICK);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //시간차이 알게
            gettimediffer();
            //갤러리,카메라 이미지 안 보이게
            viorgonelinear.setVisibility(View.GONE);
            //선택 버튼을 취소 버튼으로 바꾼다.
            imagebt.setBackgroundResource(R.drawable.add_message);
            //클릭을 했으니 된 상태라 표시
            visibleornot = false;
            //나중에 url이 들어갈 곳
            imgurl = "no";
            if (requestCode == GALLERY_PICK) {

                gettimediffer();
                imglist = new ArrayList<>();
                realpathlist = new ArrayList<>();
                if (data.getClipData() == null) {
                    //한 장 선택됬을 경우
                    Log.d(TAG, "한 개의 이미지 임시 uri " + data.getData());
                    imglist.add(data.getData());
                    String realpath = getRealPathFromURI_API19(ctx, data.getData());
                    Log.d(TAG, "한 개의 이미지 절대 경로 " + realpath);
                    Log.d(TAG, "uri로 변환 " + Uri.parse(realpath));
                    realpathlist.add(realpath);
                    /*createFilePart("",realpath);*/


                    getymdhm();
                    //다른 클라에게 이미지 전송
                    makebitmap();
                    //서버에 업로드
                    uploadimage();


                } else {

                    //사진은 10개까지 선택 가능
                    ClipData clipData = data.getClipData();

                    if (clipData.getItemCount() > 10) {
                        Toast.makeText(ctx, "사진은 10개까지 선택가능합니다.", Toast.LENGTH_SHORT).show();
                        return;

                    } else if (clipData.getItemCount() > 1 && clipData.getItemCount() < 10) {

                        Log.d(TAG, "onActivityResult:이미지가  " + clipData.getItemCount() + "개 선택됨");
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            imglist.add(clipData.getItemAt(i).getUri());
                            Log.d(TAG, "onActivityResult:path uri " + clipData.getItemAt(i).getUri());
                            Log.d(TAG, "onActivityResult:path " + clipData.getItemAt(i).getUri().getPath());
                            String realpath = getRealPathFromURI_API19(ctx, clipData.getItemAt(i).getUri());
                            Log.d(TAG, "onActivityResult:path real " + realpath);
                            realpathlist.add(realpath);

                        }
                        Log.d(TAG, "onActivityResult: 1개 uri : " + imglist.get(0));
                        //사진을 선택한 날짜와 시간을 얻는다.
                        getymdhm();
                        //서버에 이미지를 업로드 한다.
                        uploadimage();
                        //다른 클라에게 이미지 전송
                        makebitmap();

                    }
                }


            } else if (requestCode == CAMERA_PICK) {
                //이미지를 서버에 저장하고 다른 사용자에게 보내는 시간을 구한다.
                getymdhm();

                //찍은 사진을 압축한다.
                bitmap = decodeSampledBitmapFrompath(currentPhotoPath, 200, 250);

                //회전이 된 상태라면 얼마나 어디로 회전되었는지 알아본다.
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(currentPhotoPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int exifOrientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int exifDegree = exifOrientationToDegrees(exifOrientation);
                //회전 되었다면 원 상태로 복구한다.
                bitmap = rotate(bitmap, exifDegree);

                //image를 서버로 보내기 위해 string으로 바꾼다
                imagetostringforsave();


                //1. 사진을 문자열로 바꾼다.
                //2. 이미지를 byte 배열로 바꿔 다른 사용자에게 전송한다.
                imgfortransfer();
                //내 화면에 보여준다.

                Message message = new Message(type, receiveremail, myurl, base64str, imgurl, rnum, myname, ymd, hm);
                messageList.add(message);
                messagein(messageList);

            }

        } else {
            Toast.makeText(ctx, "이미지 선택을 취소했습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    private String bitmaptostring() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //bitmap을 jpeg로 압축한다.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte, Base64.DEFAULT);
    }

    private Bitmap decodeSampledBitmapFrompath(String currentPhotoPath, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height);
        Log.d(TAG, "decodeSampledBitmapFrompath: " + options.inSampleSize);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        //options.inPurgeable = true;
        //options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(currentPhotoPath, options);


    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqwidth, int reqheight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqheight || width > reqwidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqheight
                    && (halfWidth / inSampleSize) >= reqwidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void uploadimage() {
        // create list of file parts (photo, video, ...)

        List<MultipartBody.Part> parts = new ArrayList<>();
        Log.d(TAG, "uploadimage:real " + realpathlist.size() + "uri " + imglist.size());
        if (realpathlist != null) {
            // create part for file (photo, video, ...)
            for (int i = 0; i < realpathlist.size(); i++) {
                parts.add(prepareFilePart("image" + i, realpathlist.get(i)));
            }
        }
        System.out.println(parts);

        // create a map of data to pass along
        RequestBody Hm = createPartFromString(hm);
        RequestBody YMD = createPartFromString(ymd);
        RequestBody Rnum = createPartFromString(rnum);
        RequestBody senderemail = createPartFromString(myemail);
        RequestBody size = createPartFromString("" + parts.size());
        Log.d(TAG, "uploadimage:siz " + size);
        Log.d(TAG, "uploadimage: " + parts.get(0));

        Call<Messagelist> call = apiInterface.upload(senderemail, Rnum, YMD, Hm, size, parts);
        call.enqueue(new Callback<Messagelist>() {
            @Override
            public void onResponse(Call<Messagelist> call, Response<Messagelist> response) {
                Log.d(TAG, "onResponse: ");
                if (response.isSuccessful()) {

                    Messagelist messagelist = response.body();
                    //얼마나 시간 걸렸나 알기
                    gettimediffer2();
                    //message 객체를 담은 리스트인데 url를 가지고 있음
                    imgurllist = messagelist.getMessageslist();
                    Log.d(TAG, "onResponse:size " + imgurllist.size());
                    //makebitmap();
                }
            }
            @Override
            public void onFailure(Call<Messagelist> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    private void makebitmap() {
        type = "image";
        bytelist = new ArrayList<>();
        gettimediffer();
        Log.d(TAG, "makebitmap:size" + imglist.size() + "절대 " + realpathlist.size());
        if (imglist != null) {

            for (int i = 0; i < realpathlist.size(); i++) {
                Log.d(TAG, "makebitmap: " + i);

                  /*  //uri를 bitmap으로 전환
                    bitmap = MediaStore.Images.Media.getBitmaptoString(getContentResolver(), Uri.parse(realpathlist.get(i)));
                    InputStream in = getContentResolver().openInputStream(Uri.parse(realpathlist.get(i)));
                    bitmap = BitmapFactory.decodeStream(in);*/
                //bitmap을 압축
                bitmap = decodeSampledBitmapFrompath(realpathlist.get(i), 200, 250);

                //회전된 이미지 원래대로 하기
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(realpathlist.get(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int exifOrientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int exifDegree = exifOrientationToDegrees(exifOrientation);
                bitmap = rotate(bitmap, exifDegree);

                //bitmap을 바이트 배열로
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                //bitmap을 jpeg로 압축한다.
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                byte[] oneimgByte = byteArrayOutputStream.toByteArray();
                //압축된 byte배열을 리스트에 한 개씩 담는다.
                bytelist.add(oneimgByte);
                //이미지를 기기에 저장하기 위해 문자열로 바꾼다.
                base64str = Base64.encodeToString(oneimgByte, Base64.DEFAULT);
                //메세지리스트에 하나씩 추가하기
                Message message = new Message(type, receiveremail, myurl, base64str, imgurl, rnum, myname, ymd, hm);
                messageList.add(message);


            }
            //전부 bitmap을 압축하는데 걸리는 시간 구하기
            gettimediffer2();
            imgposition = 0;
            //추가된 이미지 메세지를 가지고 내 핸드폰에 보여줌
            messagein(messageList);
            transferimages();


        }
    }

    private void transferimages() {
        Log.d(TAG, "transferimages:size" + realpathlist.size());
        if (realpathlist.size() == 1) {
            //이미지가 한 개라면
            //imgurl = imgurllist.get(0).getMessage();
            imgurl = "no";
            imgByte = bytelist.get(0);
            new Sender().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Log.d(TAG, "transferimages:posi " + imgposition);
            //이미지가 여러 개라면 이미지 posi에 맞게 url과 압축된 이미지를 받는다.
            //imgurl = imgurllist.get(imgposition).getMessage();
            imgurl = "no";
            type = "image";
            imgByte = bytelist.get(imgposition);
            new Sender().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, String fileUri) {
        // create RequestBody instance from file
        File file = new File(fileUri);

        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        file
                );
        Log.d(TAG, "prepareFilePart:file name " + file.getName());
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private void createFilePart(String s, String realpath) {
        File file = new File(realpath);
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("image/*"),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("picture", file.getName(), requestFile);
        // add another part within the multipart request
        String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);
        Log.d(TAG, "createFilePart:requestfile " + requestFile);

        /*Call<ResponseBody> call = apiInterface.upload(description, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: upload success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: fail"+t);
            }
        });*/
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }


    public static String getRealPathFromURI_API19(final Context context, final Uri uri) {

        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    private String getRealPathFromURIPath(Uri contentURI) {

        String result = null;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            if (cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                cursor.close();
            }
        }
        return result;
       /*

        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }*/
    }

    private Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try {
                Log.d(TAG, "rotate: " + degrees);
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch (OutOfMemoryError ex) {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "createImageFile:새로 만든 파일의 절대 경로 " + currentPhotoPath);
        return image;
    }

    private void gettimediffer() {
        now = System.currentTimeMillis();
        date1 = new Date(now);
        time1 = f1.format(date1);
        Log.d(TAG, "time 사진을 골랐을 때 " + time1);
    }

    private void imagetostringforsave() {
        //서버에 저장할 때는 압축하지 않은 파일을 사용한다.
        bitmap = (Bitmap) BitmapFactory.decodeFile(currentPhotoPath);
        //회전이 된 상태라면 얼마나 어디로 회전되었는지 알아본다.
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(currentPhotoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);
        //회전 되었다면 원 상태로 복구한다.
        bitmap = rotate(bitmap, exifDegree);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //bitmap을 jpeg로 압축한다.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
        imgByte = byteArrayOutputStream.toByteArray();
        type = "image";
        Log.d(TAG, "imagetostringforsave:type " + type);

        //이미지 저장
        saveimage();

    }

    private void gettimediffer2() {
        now = System.currentTimeMillis();
        date2 = new Date(now);
        time2 = f1.format(date2);
        Log.d(TAG, "time 서버에 저장 완료" + time2);

        long diff = date2.getTime() - date1.getTime();
        long sec = diff / 1000;
        Log.d(TAG, "time 고른 시간과 서버에 저장 시간의 차이를 초로 표현!! " + sec);
        System.out.println(sec);
    }

    private void saveimage() {
        //저장할 시간과 날짜 얻는다.
        //getymdhm();

        //메세지 객체를 만들어 리사이클러뷰 안에 보여지주기
        //updatelist();
        type = "image";
        base64str = Base64.encodeToString(imgByte, Base64.DEFAULT);
        //서버에 이미지 저장
        Log.d(TAG, "saveimage: " + myemail + "\n" + rnum + "\n" + ymd + "\n" + hm + "\n" + type + "\n");
        Call<Message> call = apiInterface.imagesave(myemail, rnum, base64str, ymd, hm, type);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful()) {
                    Message message1 = response.body();
                    Log.d(TAG, "onResponse:message " + message1.getMessage());
                    Log.d(TAG, "onResponse:message " + message1.getResponse());
                    if (message1.getResponse().equals("success")) {
                        //상대방에게 소켓으로 보내기
                        //서버에 저장된 url
                        //  imgurl = message1.getMessage();
//                        //이미지 전달하기 위해 축소
                        //imgfortransfer();
                        //시간차이 구하기
                        gettimediffer2();
                        //전송
                        //new Sender().execute();
                    }

                } else {
                    Log.d(TAG, "onResponse: error");
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });

    }

    private void imgfortransfer() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //bitmap을 jpeg로 압축한다.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        imgByte = byteArrayOutputStream.toByteArray();
        type = "image";
        Log.d(TAG, "imagetostringforsave:type " + type);
        base64str = Base64.encodeToString(imgByte, Base64.DEFAULT);
        new Sender().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

/*    private void updatelist() {
        Log.d(TAG, "updatelist: photouri " + photouri);
        String path = getPathFromURI(photouri);
//        String filename = getFileName(photouri);
//        Log.d(TAG, "updatelist: " + path);
//        Log.d(TAG, "updatelist: " + filename);
//        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), filename);
//        Log.d(TAG, "updatelist:file: "+filename);
//        Log.d(TAG, "updatelist: "+path);
        Log.d(TAG, "updatelist: " + bitmap);

        Message messageob = new Message(myemail, rnum, photouri, bitmap, ymd, hm, type);
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        messageList.add(messageob);
        messagein(messageList);

    }*/


    public String getPathFromURI(Uri contentUri) {
        String res = "";
        String res0 = "";
        String[] proj = {MediaStore.Images.Media.DATA};
//        String[] proj = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        Log.d(TAG, "getPathFromURI: " + cursor.toString());

        if (cursor == null) {
            res = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            int column_index0 = cursor.getColumnIndex(proj[0]);
            res = cursor.getString(column_index);
            res0 = cursor.getString(column_index0);

            Log.d(TAG, "getPathFromURI:res " + res);
            Log.d(TAG, "getPathFromURI:res0 " + res0);
        }
        cursor.close();
        return res;
    }

    private void getymdhm() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy년 M월 d일 E-a K시 m분");
        Date date = new Date();
        String today = df.format(date);
        String[] todayarray = today.split("-");
        ymd = todayarray[0];
        hm = todayarray[1];
        Log.d(TAG, "onCreate: " + ymd + "시간" + hm);
    }

    private void mychatlist() {

        Call<Messagelist> call = apiInterface.messagelist(rnum, myemail, myname);
        call.enqueue(new Callback<Messagelist>() {
            @Override
            public void onResponse(Call<Messagelist> call, Response<Messagelist> response) {
                if (response.isSuccessful()) {
                    Messagelist messagelist = response.body();
                    Log.d(TAG, "onResponse: " + messagelist.getResponse());
                    //서버에도 저장된 메세지가 없다면 즉 처음이라면 아무것도 안보여줌
                    if (messagelist.getMessageslist() != null) {
                        //서버에 저장된 메세지가 있다면
                        if (messageList == null) {
                            messagein(messagelist.getMessageslist());
                        } else if (messagelist.getMessageslist().size() != messageList.size()) {
                            Log.d(TAG, "onResponse:서버에서 채팅내용 받아옴 " + messagelist.getMessageslist().size());
                            Log.d(TAG, "onResponse:기기의 채팅내용 사이즈 " + messageList.size());
                            //서버에 메세지가 추가로 저장된 것이 있다면
                            messagein(messagelist.getMessageslist());
                            //서버에서 받은 메세지 리스트 로컬에 저장
                        }
                        //savemessagelist(messagelist.getMessageslist());

                        //대화방에 친구를 추가한 경우
                        if (getIntent().getStringExtra("add") != null) {
                            //친구를 채팅방에 추가했을 경우
                            //type이 초대된 사람들 이름
                            type = receiver;
                            //날짜 시간 얻는다.
                            getymdhm();
                            message = "^___join___^";
                            //추가 메세지를 나를 포함한 모든 사람들에게 보냄
                            new Sender().execute();
                        }
                    }

                } else {
                    Log.d(TAG, "onResponse: error");
                }
            }

            @Override
            public void onFailure(Call<Messagelist> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    /*private void inmylocalmessage() {
        messageList = new ArrayList<>();
        sharedPreferences = getSharedPreferences(rnum, MODE_PRIVATE);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Message>>() {
        }.getType();
        String stmesslist = sharedPreferences.getString("Message", "");

        messageList = gson.fromJson(stmesslist, type);

        if (messageList == null){
            //로컬에 저장된 것이 없으면 서버에서 받아온다.
            mychatlist();
        }else {
            Log.d(TAG, "savemessagelist: " + messageList.size());
            //메세지 보여주기
            showmessagelist(messageList);
        }
    }*/


    private void showmessagelist(ArrayList<Message> Messagelist) {

        messageList = Messagelist;
        if (Messagelist != null) {
            messageAdapter = new MessageAdapter(ctx, messageList, this);
            recyclerView.setAdapter(messageAdapter);
            layoutManager.scrollToPosition(messageList.size() - 1);
//            new Handler().postDelayed(() -> recyclerView.scrollToPosition(messageAdapter.getItemCount()-1), 1000);
            Log.d(TAG, "showmessagelist:size " + Messagelist.size());
            Log.d(TAG, "showmessagelist:size " + messageAdapter.getItemCount());
        }
    }

   /* //1. 처음에 서버에서 메세지 받아올 때
    //2. 메세지를 보낼 때(텍스트, 이미지 하나, 여러 개 보낼 때마다)
    private void savemessagelist(ArrayList<Message> messlist) {
        if (messlist != null) {
            sharedPreferences = getSharedPreferences(rnum, MODE_PRIVATE);
            editor = sharedPreferences.edit();
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Message>>() {
            }.getType();
            Log.d(TAG, "savemessagelist: " + messlist.size());
            String strmessagelist = gson.toJson(messlist, type);
            editor.putString("Message", strmessagelist);
            editor.apply();
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        //채팅방 리스트에서 채팅방  클릭해서 온 경우 rnum을 가져온다.
        //하지만 유저 프로필에서  경우엔 rnum을 가져오지 못한다.
        if (getIntent().getStringExtra("rnum") != null) {
            rnum = getIntent().getStringExtra("rnum");
            //받는 사람
            receiveremail = getIntent().getStringExtra("email");
            receiver = getIntent().getStringExtra("name");
            Log.d(TAG, "onResume: " + receiveremail);
            total = getIntent().getStringExtra("total");
            Log.d(TAG, "onResume: " + rnum);
            Log.d(TAG, "onResume: " + receiver);
            Log.d(TAG, "onResume: " + total);

        }

        //앱바 설정
        actionBar = getSupportActionBar();
        Log.d(TAG, "onResume:total " + total);
        //대화 상대방의 수에 따라 앱바의 제목을 다르게 설정
        if (total.equals("1")) {
            //한 명이라면 그 사람의 이름 설정
            actionBar.setTitle(receiver);
        } else if (total.equals("0")) {
            //없다면 알 수 없음으로 제목으로 설정
            String title = "알 수 없음";
            actionBar.setTitle(title);
        } else {
            //2명 이상이라면 그룹채팅에 나를 포함한 총 인원 수를 제목으로 설정
            int num = Integer.valueOf(total) + 1;
            String title = "그룹 채팅 " + num;
            actionBar.setTitle(title);
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // Bind to the service
        bindService(new Intent(this, TalkService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause:bound " + mBound);
        try {
            String notchatroom = "givemenoti";
            android.os.Message message = android.os.Message.obtain(null, TalkService.MSG_SET_RNUM, notchatroom);
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //imm.showSoftInput(messageedit, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        Log.d(TAG, "onStop: ");
        if (mBound) {
            Log.d(TAG, "onStop:unbind ");
            //채팅방이 아닌 다른 화면이나 앱 바깥에서 알람을 받을 수 있게 서비스에게 채팅방이 아니라고 알려준다.
            unbindService(mConnection);
            mBound = false;
            Log.d(TAG, "onStop:bound " + mBound);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rightsidemenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override   //앱바에서 지도, 뒤로가기 클릭할 때
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //메인으로
                Intent intent = new Intent(MessageActivity.this, ChatroomActivity.class);
                startActivity(intent);
                return true;
            case R.id.getout_message:
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                builder.setTitle("채팅방 나가기");
                builder.setMessage("채팅방을 나가면 채팅방 목록에서 채팅방이 삭제됩니다.")
                        .setPositiveButton("나가기", (dialog, which) -> {

                            //채팅방에서 나가거나 삭제함
                            gooutchatroom();
                        })
                        .setNegativeButton("취소", ((dialog, which) -> dialog.cancel()
                        ));
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            case R.id.add_friend_message:
                type = "friend";
                Intent addintent = new Intent(MessageActivity.this, ChooseFriends.class);
                addintent.putExtra("email", receiveremail);
                addintent.putExtra("name", receiver);
                addintent.putExtra("total", total);
                addintent.putExtra("rnum", rnum);
                startActivity(addintent);


                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    private void gooutchatroom() {

        getymdhm();

        Log.d(TAG, "gooutchatroom: " + total);

            /*//기기에서 채팅방 내용 다 지움
            editor = sharedPreferences.edit();
            editor.remove("Message");
            editor.apply();
*/

        Call<Room> call = apiInterface.getoutroom(myemail, rnum, total, ymd, hm);
        call.enqueue(new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                if (response.isSuccessful()) {
                    Room room = response.body();
                    //다른 채팅 상대방에게 나갔다고 알려줌
                    message = "^___goout___^";
                    type = "mtext";
                    new Sender().execute();
                    Log.d(TAG, "onResponse:getout " + room.getResponse());
                    Log.d(TAG, "onResponse:getout " + room.getYourname());
                    Intent intent1 = new Intent(MessageActivity.this, ChatroomActivity.class);
                    startActivity(intent1);
                } else {
                    Log.d(TAG, "onResponse: error");
                }
            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }


    private void messagein(ArrayList<Message> messagelist) {

//        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();

        //어뎁터를 초기화 한다.

        Log.d(TAG, "messagein: new message " + messagelist.size());
        messageList = messagelist;
        messageAdapter = new MessageAdapter(ctx, messageList, this);
        if (messagelist != null) {
            recyclerView.setAdapter(messageAdapter);
            messageAdapter.notifyDataSetChanged();
            recyclerView.getLayoutManager().scrollToPosition(messagelist.size() - 1);


//            if (firstmessage.equals("first")) {
//                Log.d(TAG, "messagein: first");

//                //어뎁터가 메세지가 처음인건지 추가된건지 알기 위해
//                firstmessage = "not";
//            } else {
//                Log.d(TAG, "messagein: not");

//            new Handler().postDelayed(() -> recyclerView.scrollToPosition(messagelist.size()-1), 200);
            // Restore state
//            recyclerView.smoothScrollToPosition(messagelist.size()-1);


//               layoutManager.scrollToPosition(messagelist.size()-1);
//            new Handler().postDelayed(() -> recyclerView.scrollToPosition(messageAdapter.getItemCount()-1), 200);
// }
        } else {
            Log.d(TAG, "messagein: size0");
        }

    }


    private class Sender extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: hi i'm item_message_sender");

            //사용자 각각 알림에 나를 제외한 사용자를 넣기 위해
            try {
                Log.d(TAG, "doInBackground: type " + type);
                Log.d(TAG, "doInBackground: total " + total);
                Log.d(TAG, "doInBackground: total " + receiveremail);
                Log.d(TAG, "doInBackground: total " + message);
                Log.d(TAG, "doInBackground: total " + rnum);
                Log.d(TAG, "doInBackground: total " + myname);
                Log.d(TAG, "doInBackground: total " + myurl);
                Log.d(TAG, "doInBackground: total " + ymd);
                Log.d(TAG, "doInBackground: total " + hm);

                TalkService.out.writeUTF(type);
                TalkService.out.writeUTF(total);
                TalkService.out.writeUTF(myemail);
                TalkService.out.writeUTF(receiveremail);
                TalkService.out.writeUTF(rnum);
                TalkService.out.writeUTF(myname);
                TalkService.out.writeUTF(myurl);
                TalkService.out.writeUTF(ymd);
                TalkService.out.writeUTF(hm);

                if (type.equals("image")) {
                    TalkService.out.writeUTF(imgurl);
                    TalkService.out.writeInt(imgByte.length);
                    TalkService.out.write(imgByte, 0, imgByte.length);

                    Log.d(TAG, "doInBackground:잘보냄  " + imgByte.length);
                    Log.d(TAG, "doInBackground: imageurl도 " + imgurl);
                } else {
                    TalkService.out.writeUTF(message);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground:error " + e.getMessage());
            }
            Log.d(TAG, "doInBackground: end");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //savemessagelist(messageList);
            if (imglist != null) {
                //1. 갤러리를 통해 이미지 전송한 경우, 텍스트나 카메라 전송은 해당하지 않는다.
                //2. 2개 이상 보낸 경우 이미지에 대한 url과 byte 배열 다시 받고, 다시 실행
                if (imglist.size() >= 2) {

                    //이미지를 하나 보냈으므로 나의 채팅리스트에 이미지를 하나 추가해준다.
                    if (imglist.size() - 1 == imgposition) {
                        return;
                    }
                    imgposition = imgposition + 1;
                    transferimages();
                }
            }
        }
    }


}
