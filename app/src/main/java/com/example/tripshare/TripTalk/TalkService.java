package com.example.tripshare.TripTalk;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TalkService extends Service {
    public static Socket socket;
    DataInputStream in;
    public static DataOutputStream out;
    private static final String TAG = "TalkService";
    Context context = this;
    /**
     * Command to the service to display a message
     */
    static final int MSG_SAY_HELLO = 1;

    public static final String CHAT_CHANNEL_ID = "chatchannelid";
    public static final String CHANNEL_DESCRIPTION = "메세지가 올 경우 알림을 받습니다.";
    public static final String CHANNEL_NAME = "tripshare에서 온 메세지";
    public static final int MSG_REGISTER_CLIENT = 44;
    static final int MSG_UNREGISTER_CLIENT = 88;
    public static final int MSG_SET_RNUM = 77;
    public static final int MSG_SET_RECEIVERS_EMAIL = 99;
    public static final int IMG_SET_RNUM = 100;
    static final int MSG_SEND_SAVE = 111;
    private String checkroom, receiversemail;
    ArrayList<Messenger> mClients;
    NotificationCompat.Builder builder;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private String myemail, myimgurl;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    Log.d(TAG, "handleMessage: ");
                    mClients = new ArrayList<>();
                    mClients.add(msg.replyTo);
                    break;
                case MSG_SET_RNUM:
                    Log.d(TAG, "handleMessage:checkroom obj "+msg.obj);
                    checkroom = String.valueOf(msg.obj);
                    Log.d(TAG, "handleMessage:checkroom " + checkroom);
                    break;
                case MSG_SET_RECEIVERS_EMAIL:
                    receiversemail = String.valueOf(msg.obj);
                    Log.d(TAG, "handleMessage:receivers email " + receiversemail);
                    break;
                case MSG_SEND_SAVE:
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */

    private void imagetoChatroomA(String type, String senderemail, String receiveremail, byte[] bytes, String rnum, String sendername,
                                  String senderurl, String ymd, String hm, String imgurl) {
        Log.d(TAG, "messagetoChatroomA: " + receiveremail);
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                String imgtostring = Base64.encodeToString(bytes, Base64.DEFAULT);
                Log.d(TAG, "imagetoChatroomA: sender" + imgtostring.length());
                com.example.tripshare.Data.Message message1 = new com.example.tripshare.Data.Message(type, receiveremail, senderurl, imgtostring, imgurl, rnum, sendername, ymd, hm);
                //메세지를 저장한다.
                //savemessage(message1, rnum);
                mClients.get(i).send(Message.obtain(null, IMG_SET_RNUM, message1));
            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

    private void messagetoChatroomA(String type, String senderemail, String receiveremail, String message, String rnum, String sendername, String senderurl, String ymd, String hm) {
        Log.d(TAG, "messagetoChatroomA: " + receiveremail);
        Log.d(TAG, "messagetoChatroomA:senderemail " + senderemail);
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {

                Log.d(TAG, "messagetoChatroomA:message " + message);

                if (message.equals("^___join___^")) {

                    //초대 메세지인 경우
                    //보내는 사람 이메일에 누구를 초대했는지에 대한 정보를 넣는다.
                    //총 추가된 사람을 포함한 총 인원 수
                    String[] emails = receiveremail.split(",");
                    String total = String.valueOf(emails.length + 1);
                    Log.d(TAG, "messagetoChatroomA: " + total);
                    String plusemails = "";
                    //받는 사람의 이메일중 내 이메일을 제외한다.
                    for (int num = 0; num < emails.length; num++) {

                        if (emails[num].equals(myemail)) {
                            //받는 사람 이메일 중 하나가 내 이메일이라면
                            //아무것도 안한다.
                        } else {
                            //받는 사람 이메일중 하나가 내 이메일이 아닌 경우
                            //새로운 이메일 리스트에 넣어준다.
                            plusemails = emails[num] + "," + plusemails;
                        }
                    }
                    Log.d(TAG, "messagetoChatroomA: plusemails : " + plusemails);
                    String newemailforsend = plusemails.substring(0, plusemails.length() - 1);
                    Log.d(TAG, "messagetoChatroomA:sub " + newemailforsend);


                    //사람을 초대했을 경우
                    //추가된 사람들을 메세지로 보낸다. newemailforsend
                    com.example.tripshare.Data.Message message1 = new com.example.tripshare.Data.Message(type, newemailforsend, senderurl, message, rnum, sendername, ymd, hm);
                    message1.setTotal(total);
                    mClients.get(i).send(Message.obtain(null, MSG_SET_RNUM, message1));
                    //메세지를 저장한다.
                    //savemessage(message1, rnum);
                } else if (message.equals("^___goout___^")) {
                    String[] emails = receiveremail.split(",");
                    String total = String.valueOf(emails.length-1);
                    Log.d(TAG, "messagetoChatroomA: " + total);

                    //채팅방을 나갔을 경우
                    com.example.tripshare.Data.Message message1 = new com.example.tripshare.Data.Message(type, senderemail, senderurl, message, rnum, sendername, ymd, hm);
                    message1.setTotal(total);
                    //바뀐 채팅방의 사람의 수를 변경해준다.
                    mClients.get(i).send(Message.obtain(null, MSG_SET_RNUM, message1));

                } else {
                    //초대가 아닐 경우 (텍스트,이미지)
                    //보낸 사람의 이메일도 같이 보낸다. senderemail
                    com.example.tripshare.Data.Message message1 = new com.example.tripshare.Data.Message(type, senderemail, senderurl, message, rnum, sendername, ymd, hm);
                    //바뀐 채팅방의 사람의 수를 변경해준다.
                    mClients.get(i).send(Message.obtain(null, MSG_SET_RNUM, message1));
                    //메세지를 저장한다.
                    //savemessage(message1, rnum);
                }

            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

    public TalkService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        connect.start();
        return START_STICKY;


    }

    Thread connect = new Thread() {
        @Override
        public void run() {
            //처음에 연결할 때에만 no가 됨
            checkroom = "no";
            PrefConfig prefConfig = new PrefConfig(context);
            try {
                socket = new Socket("115.71.238.81", 8888);    //서버로 접속

                Log.d(TAG, "run:connect server ");
                in = new DataInputStream(socket.getInputStream());
//                in2 = new BufferedReader(new InputStreamReader(System.in));
                Log.d(TAG, "run: send outputstream before");
                out = new DataOutputStream(socket.getOutputStream());
                Log.d(TAG, "run: send thread start");
                //아이디 보낸다.
                myemail = prefConfig.readEmail();
                myimgurl = prefConfig.readimgurl();
                Log.d(TAG, "run:myemail " + myemail);
                out.writeUTF(myemail);

                while (true) {
                    String type = in.readUTF();
                    String senderemail = in.readUTF();
                    String receiveremail = in.readUTF();
                    String rnum = in.readUTF();
                    String sendername = in.readUTF();
                    String senderurl = in.readUTF();
                    String ymd = in.readUTF();
                    String hm = in.readUTF();
                    String total = in.readUTF();
                    Log.d(TAG, "run:senderemail " + senderemail);
                    Log.d(TAG, "run:receiveremail " + receiveremail);
                    Log.d(TAG, "run:rnum " + rnum);
                    Log.d(TAG, "run:type " + type);
                    if (type.equals("image")) {
                        //이미지 url과 사진 bitearray를 받는다.
                        String imgurl = in.readUTF();
                        int len = in.readInt();
                        Log.d(TAG, "run:len " + len);

                        byte[] data = new byte[len];
                        if (len > 0) {
                            Log.d(TAG, "run:imgcheckroom " + checkroom);
                            Log.d(TAG, "run:imgrnum " + rnum);
                            in.readFully(data, 0, data.length);
                            if (checkroom.equals(rnum)) {
                                //사용자가 현재 메세지를 받은 채팅방에 위치함
                                //알람 x 메세지 o

                                imagetoChatroomA(type, senderemail, receiveremail, data, rnum, sendername, senderurl, ymd, hm, imgurl);
                            } else if (checkroom.equals("roomlist")) {
                                //사용자가 현재 채팅방 목록에 위치함
                                //알람 o with 메세지, 메세지 o
                                String fornoti = "smessage";
                                makenotification(type, senderemail, sendername, fornoti, senderurl, rnum, total, receiveremail);
                                imagetoChatroomA(type, senderemail, receiveremail, data, rnum, sendername, senderurl, ymd, hm, imgurl);

                            } else {
                                //사용자가 채팅방 목록 이외의 다른 곳에 위치함(내 앱 내부,앱 외부)
                                //알람 o with 메세지 o, 메세지 x
                                String fornoti = "smessage";
                                makenotification(type, senderemail, sendername, fornoti, senderurl, rnum, total, receiveremail);
                            }


                        } else {

                            Log.d(TAG, "run:이미지 못받음  ");
                        }
                    } else {
                        String message = in.readUTF();
                        //현재 있는 곳이 채팅방리스트이면(="no") 채팅방 리스트에 메세지를 보낸다. 알람도 같이 보낸다.
                        //사용자가 현재 있는 채팅방과 메세지를 받은 채팅방이 같은지 비교를 해서
                        //같으면 메세지를 보낸다.
                        //다르면 알람을 띄워준다. 메세지는 알람에 같이 보낸다.

                        Log.d(TAG, "run:text check rnum " + rnum);
                        Log.d(TAG, "run:text check checkroom " + checkroom);
                        if (message.equals("videocall")) {
                            //영상통화인 경우
                            choosetoreceive(sendername, senderemail, senderurl, rnum);
                        } else if (message.equals("endvideocall")) {
                            endvideochat();
                        } else if (message.equals("declinevideocall")) {
                            //상대방이 영상통화 거절한 경우
                            declinemessage(sendername);
                        } else if (checkroom.equals(rnum)) {
                            //사용자가 현재 메세지를 받은 채팅방에 위치함
                            //알람 x 메세지 o
                            Log.d(TAG, "run:no noti equal rnum ");
                            messagetoChatroomA(type, senderemail, receiveremail, message, rnum, sendername, senderurl, ymd, hm);
                        } else if (checkroom.equals("roomlist")) {
                            //사용자가 현재 채팅방 목록에 위치함
                            //알람 o with 메세지, 메세지 o
                            Log.d(TAG, "run: roomlist");
                            makenotification(type, senderemail, sendername, message, senderurl, rnum, total, receiveremail);
                            messagetoChatroomA(type, senderemail, receiveremail, message, rnum, sendername, senderurl, ymd, hm);

                        } else {
                            Log.d(TAG, "run: only noti ");
                            //사용자가 채팅방 목록 이외의 다른 곳에 위치함(내 앱 내부,앱 외부)
                            //알람 o with 메세지 o, 메세지 x
                            makenotification(type, senderemail, sendername, message, senderurl, rnum, total, receiveremail);
                        }
                    }

                }


            } catch (IOException e) {
                Log.d(TAG, "run: " + e.getMessage());
                e.printStackTrace();
            }

        }
    };

    private void endvideochat() {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                String sendername = "out";
                mClients.get(i).send(Message.obtain(null, MSG_SET_RNUM, sendername));
            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

    private void declinemessage(String sendername) {
        Log.d(TAG, "messagetoChatroomA: " + sendername);
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i).send(Message.obtain(null, IMG_SET_RNUM, sendername));
            } catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

    private void choosetoreceive(String sendername, String senderemail, String senderurl, String rnum) {
        Log.d(TAG, "choosetoreceive: " + senderemail);
        Log.d(TAG, "choosetoreceive: " + sendername);
        Log.d(TAG, "choosetoreceive: " + senderurl);
        Log.d(TAG, "choosetoreceive: " + rnum);
        Intent intent = new Intent(this, VideoCallActivity.class);
        intent.putExtra("sendername", sendername);
        intent.putExtra("senderemail", senderemail);
        intent.putExtra("rnum", rnum);
        intent.putExtra("senderurl", senderurl);
        startActivity(intent);

    }

    /*private void savemessage(com.example.tripshare.Data.Message message, String rnum) {
        //저장된 채팅방의 메세지 리스트를 가져온다.
        SharedPreferences sharedPreferences = getSharedPreferences(rnum, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Message", "");
        Type type = new TypeToken<ArrayList<com.example.tripshare.Data.Message>>() {
        }.getType();
        Log.d(TAG, "savemessage: " + type.toString());
        //내 기기에 저장된 이전 메세지들 가져오기
        ArrayList<com.example.tripshare.Data.Message> messageslist = gson.fromJson(json, type);

        if (messageslist ==null){
            //채팅방을 만들고 처음 메세지를 받은 경우
            ArrayList<com.example.tripshare.Data.Message> newmessagelist = new ArrayList<>();
            //메세지 리스트에 추가한다.
            newmessagelist.add(message);
            //메세지 리스트를 다시 기기에 저장한다.
            String tojson = gson.toJson(messageslist, type);
            editor.putString("Message", tojson);
            editor.apply();
            return;
        }
        Log.d(TAG, "savemessage:li size " + messageslist.size());
        //메세지 리스트에 서버에게 받은 메세지를 추가한다.
        messageslist.add(message);
        //메세지 리스트를 다시 기기에 저장한다.
        String tojson = gson.toJson(messageslist, type);
        editor.putString("Message", tojson);
        editor.apply();
    }*/

    private void makenotification(String type, String senderemail, String name, String message, String imgurl,
                                  String rnum, String total, String receiveremail) {
        Log.d(TAG, "makenotification: message " + message);
        if (!message.equals("^___goout___^") && !message.equals("^___join___^")
                && !imgurl.equals(myimgurl)) {
            Log.d(TAG, "makenotification:receiveremail " + receiveremail);

            //채팅방의 모든 이메일 중에서 다른 사람들의 이메일만 가져오기
            String othersemail = othersemail(receiveremail, senderemail, total);
            Log.d(TAG, "makenotification:othersemail " + othersemail);
            Log.d(TAG, "makenotification: " + rnum);
            Log.d(TAG, "makenotification: " + total);
            Log.d(TAG, "makenotification: " + name);
            //알림의 구체적인 내용 설정, 채널도 설정
            builder = new NotificationCompat.Builder(context, CHAT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_action_name)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle(name);

            //이미지인지 텍스트인지 구별
            if (type.equals("image")) {
                builder.setContentText("이미지를 전송했습니다.");
            } else {
                builder.setContentText(message);
            }

            //알림에 담을 인텐트 내용 설정
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("rnum", rnum);
            intent.putExtra("total", total);
            intent.putExtra("name", name);
            intent.putExtra("email", othersemail);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            //알림에 intent를 넣을  pendingintent를 설정하고 알림에 넣어줌
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            //설정된 알림을 사용하기 위한 알림 매니저
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            //url에 해당하는 이미지를 http통신을 사용해 가져온 다음
            //bitmap으로 바꾼다. 해당 bitmap을 이미지 bitmap으로 바꿔서 알람에 설정한다.
            Glide.with(context)
                    .asBitmap()
                    .load(imgurl)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                            Bitmap bitmap = getCircleBitmap(resource);
                            Log.d(TAG, "onResourceReady: " + resource);
                            builder.setLargeIcon(bitmap);
                            if (notificationManager != null) {
                                notificationManager.notify(2, builder.build());
                                Log.d(TAG, "makenotification: ");
                            }
                            Log.d(TAG, "makenotification: ");
                        }
                    });
        }


    }

    private String othersemail(String receiveremail, String senderemail, String total) {
        Log.d(TAG, "othersemail:all,my " + myemail + "\n" + receiveremail);
        String[] emails = receiveremail.split(",");
        //자바 배열은 특정 값를 제거하는 함수를 제공하지 않아서 리스트로 바꿔서 제거함
        List<String> list = new ArrayList<>(Arrays.asList(emails));
        Log.d(TAG, "othersemail: " + senderemail);
        String othersemail = senderemail + ",";
        for (int i = 0; i < emails.length; i++) {
            if (emails[i].equals(myemail)) {
                list.remove(emails[i]);
                break;
            }
        }
        //다른 사람들의 이메일을 가진 문자열 만듬
        for (int i = 0; i < list.size(); i++) {
            othersemail = othersemail + list.get(i) + ",";
        }
        othersemail = othersemail.substring(0, othersemail.length() - 1);
        Log.d(TAG, "othersemail:othersemail " + othersemail);
        return othersemail;
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2,
                bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy:super앞 ");

        super.onDestroy();
        Log.d(TAG, "onDestroy:super뒤 ");
//        stopForeground(true);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
//        startForeground(23, new Notification());
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        //IBinder 객체를 TalkService에 bind요청한 클라이언트로 전달
        return mMessenger.getBinder();
    }
}
