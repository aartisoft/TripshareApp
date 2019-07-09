package com.example.tripshare.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tripshare.Data.Message;
import com.example.tripshare.R;
import com.example.tripshare.TripTalk.RecyclerViewClickListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.tripshare.TripTalk.MessageActivity.myurl;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MeesageViewholder> {

    private Context mctx;
    private ArrayList<Message> messageslist;

    private static final String TAG = "MessageAdapter";
    private static final int MSG_LEFT = 0;
    private static final int MSG_RIGHT = 1;
    int clickposi;
    private static int viewtype;
    private static RecyclerViewClickListener itemlistener;


    public MessageAdapter(Context mctx, ArrayList<Message> messageslist,RecyclerViewClickListener itemlistener) {
        this.mctx = mctx;
        this.messageslist = messageslist;
        this.itemlistener = itemlistener;

    }

    @NonNull
    @Override
    public MeesageViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Log.d(TAG, "onCreateViewHolder:viewtype " + viewtype);
        Log.d(TAG, "onCreateViewHolder:i " + i);
        if (i == MSG_RIGHT) {

            //내가 보낸 메세지라면
            LayoutInflater layoutInflater = LayoutInflater.from(mctx);
            View view = layoutInflater.inflate(R.layout.item_message_sender, viewGroup, false);
            return new MeesageViewholder(view);
        } else {
            //상대방이 보낸 것이라면
            LayoutInflater layoutInflater = LayoutInflater.from(mctx);
            View view = layoutInflater.inflate(R.layout.itme_mesasaged_received, viewGroup, false);
            return new MeesageViewholder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType:position " + position);
        if (messageslist.get(position).getImgurl().equals(myurl)) {
            //내가 보낸 것이라면
            viewtype = MSG_RIGHT;
            return MSG_RIGHT;
        } else {
            viewtype = MSG_LEFT;
            //내가 보낸 것이 아니라면
            return MSG_LEFT;
        }


    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull MeesageViewholder meesageViewholder, int i) {
        Log.d(TAG, "onBindViewHolder:viewtype " + viewtype);
        if (viewtype == MSG_RIGHT) {
            //내가 보낸 메세지라면
            Log.d(TAG, "onBindViewHolder: " + messageslist.get(i).getRnum());

            //날짜 구분
            if (i == 0) {
                //메세지가 하나이거나
                //오늘 보낸 것이랑 어제 보낸 것의 날짜가 다르면 경계선 그어주고 오늘 날짜 보여주기
                meesageViewholder.sendlinear.setVisibility(View.VISIBLE);
                meesageViewholder.sendymdTx.setText(messageslist.get(i).getYmd());
            } else if (!messageslist.get(i).getYmd().equals(messageslist.get(i - 1).getYmd())) {
                meesageViewholder.sendlinear.setVisibility(View.VISIBLE);
                meesageViewholder.sendymdTx.setText(messageslist.get(i).getYmd());

            } else {
                meesageViewholder.sendlinear.setVisibility(View.GONE);
            }

            //시간 구분
            if (i == messageslist.size() - 1) {
                //처음에는 시간을 넣어준다.
                meesageViewholder.stimetx.setVisibility(View.VISIBLE);
                meesageViewholder.stimetx.setText(messageslist.get(i).getHm());
            } else if (messageslist.get(i).getHm().equals(messageslist.get(i + 1).getHm()) && messageslist.get(i).getSendername().equals(messageslist.get(i + 1).getSendername())) {
                //새로운 메세지와 다음 메세지가 시분이 같고, 이전 메세지와 이름이 같다면 새로운 메세지의 시간을 안보이게 한다.
                meesageViewholder.stimetx.setVisibility(View.GONE);
            } else {
                //시분이 다른 경우 새로운 메세지의 시분을 보이게 한다.
                meesageViewholder.stimetx.setVisibility(View.VISIBLE);
                meesageViewholder.stimetx.setText(messageslist.get(i).getHm());
            }


            if (messageslist.get(i).getMessage().equals("^___join___^")) {
                //사용자가 누구를 초대한 경우
                // 텍스트 메세지 넣는 곳이랑 시간 안보이게
                meesageViewholder.semessagetx.setVisibility(View.GONE);
                meesageViewholder.stimetx.setVisibility(View.GONE);
                meesageViewholder.sendimgv.setVisibility(View.GONE);
                meesageViewholder.sendimgtimetx.setVisibility(View.GONE);
                //초대메세지 레이아웃 보이게
                meesageViewholder.invitelinear.setVisibility(View.VISIBLE);

                //누가 누구를 초대 했다는 메세지를 만들고 텍스트 뷰에 넣어줌
                Log.d(TAG, "onBindViewHolder:sender email "+messageslist.get(i).getSenderemail());
                String join = messageslist.get(i).getSendername() + "님이 " + messageslist.get(i).getType() + "님(들)을 채팅방으로 초대했습니다. ";
                Log.d(TAG, "onBindViewHolder: " + join);
                meesageViewholder.invitetx.setText(join);

            } else if (messageslist.get(i).getType().equals("mtext")) {
                //메세지가 이미지인지 텍스트인지 구분
                //텍스트인 경우
                //이미지랑 이미지 보낸 시간 안보이게
                //초대메세지 레이아웃 안 보이게
                meesageViewholder.invitelinear.setVisibility(View.GONE);
                meesageViewholder.sendimgtimetx.setVisibility(View.GONE);
                meesageViewholder.sendimgv.setVisibility(View.GONE);


                //텍스트 메세지 온 경우 바로 넣어줌
                meesageViewholder.semessagetx.setText(messageslist.get(i).getMessage());

            } else if (messageslist.get(i).getType().equals("image")) {
                //보낼 때는 uri를 사용한다.
                // 텍스트 넣는 곳이랑 시간 안보이게
                //초대메세지 레이아웃 안 보이게
                meesageViewholder.invitelinear.setVisibility(View.GONE);
                meesageViewholder.semessagetx.setVisibility(View.GONE);
                meesageViewholder.stimetx.setVisibility(View.GONE);
                //이미지, 이미지 시간 레이아웃 보이게

                meesageViewholder.sendimgv.setVisibility(View.VISIBLE);
                meesageViewholder.sendimgtimetx.setVisibility(View.VISIBLE);
                Log.d(TAG, "onBindViewHolder: mytime: " + messageslist.get(i).getHm());
                Log.d(TAG, "onBindViewHolder:uri " + messageslist.get(i).getPhotouri());
//                Log.d(TAG, "onBindViewHolder:path" +messageslist.get(i).getMessage());
//                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), messageslist.get(i).getMessage());
//                Log.d(TAG, "onBindViewHolder:uripath new file "+file);
//                Log.d(TAG, "onBindViewHolder:uripath new file uri "+ Uri.fromFile(file).toString());
                //이미지 넣어주기
//                Log.d(TAG, "onBindViewHolder: "+messageslist.get(i).getMessage());
//                Glide.with(mctx).load(Uri.fromFile(file).getPath()).into(meesageViewholder.sendimgv);
                Log.d(TAG, "onBindViewHolder:bitmap " + messageslist.get(i).getBitmaptoString());

                //저장된 이미지를 불러오는가? 아니면 저장된 이미지를 불러오는가?
                if (messageslist.get(i).getBitmaptoString() != null) {
                    //방금 메세지를 받은 경우
                    Log.d(TAG, "onBindViewHolder:bitmaptostring "+messageslist.get(i).getBitmaptoString().length());
                    byte[] decodedString = Base64.decode(messageslist.get(i).getBitmaptoString(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    Glide.with(mctx).load(decodedByte).into(meesageViewholder.sendimgv);
                } else {
                    Log.d(TAG, "onBindViewHolder:url " + messageslist.get(i).getMessage());
                    //저장된 이미지 불러오기
                    Glide.with(mctx).load(messageslist.get(i).getMessage()).centerCrop().thumbnail(0.6f).into(meesageViewholder.sendimgv);
                }
              /* *//* //이미지 클릭 이벤트
                meesageViewholder.sendimgv.setOnClickListener(v -> {
                    //이미지가 있는 MESSAGE 모으기
                    ArrayList<Message> messagesli = new ArrayList<>();
                    for (int img = 0; img < messageslist.size(); img++) {
                        if (messageslist.get(img).getType().equals("image")) {
                            messagesli.add(messageslist.get(img));
                        }
                    }*//*

                    모은 message중에서 사용자가 클릭한 이미지가 새로운 리스트에서 몇 번째에 있는지 찾기
                    for (int posi = 0; posi < messagesli.size(); posi++) {
                        if (messagesli.get(posi).getMessage().equals(messageslist.get(i).getMessage())) {
                            clickposi = posi;
                            break;
                        }
                    }*//*

                    //전달
                    Intent intent = new Intent(mctx, PictureActivity.class);
                    intent.putExtra("clickposition", clickposi);
                    intent.putExtra("rnum", messageslist.get(i).getRnum());
                    mctx.startActivity(intent);

                });*/


                meesageViewholder.sendimgtimetx.setText(messageslist.get(i).getHm());
            }

        } else {

            Log.d(TAG, "onBindViewHolder:상대방이초대 " + messageslist.get(i).getMessage());
            Log.d(TAG, "onBindViewHolder:상대방이초대 " + messageslist.get(i).getMessage().equals("^___join___^"));
            //상대방이 보낸 메세지라면
            if (messageslist.get(i).getMessage().equals("^___goout___^")) {

                //날짜 구분
                if (i == 0) {
                    //메세지가 하나이거나
                    //오늘 보낸 것이랑 어제 보낸 것의 날짜가 다르면 경계선 그어주고 오늘 날짜 보여주기
                    meesageViewholder.receilinear.setVisibility(View.VISIBLE);
                    meesageViewholder.receiymdtx.setText(messageslist.get(i).getYmd());
                } else if (!messageslist.get(i).getYmd().equals(messageslist.get(i - 1).getYmd())) {
                    meesageViewholder.receilinear.setVisibility(View.VISIBLE);
                    meesageViewholder.receiymdtx.setText(messageslist.get(i).getYmd());
                } else {
                    meesageViewholder.receilinear.setVisibility(View.GONE);
                }

                //상대방이 나갔다면
                meesageViewholder.gooutlinear.setVisibility(View.VISIBLE);
                String goout = messageslist.get(i).getSendername() + "님이 채팅방을 나갔습니다.";
                meesageViewholder.goouttx.setText(goout);

                //프로필 사진,이름이랑 메세지 안보이게
                meesageViewholder.renametx.setVisibility(View.INVISIBLE);
                meesageViewholder.remessagetx.setVisibility(View.INVISIBLE);
                meesageViewholder.recirimg.setVisibility(View.GONE);
                meesageViewholder.receimgv.setVisibility(View.GONE);
                meesageViewholder.receimgtimetx.setVisibility(View.GONE);

            } else if (messageslist.get(i).getMessage().equals("^___join___^")) {
                //새로운 사용자가 초대 되었다면


                if (i == 0) {
                    //메세지가 하나이거나
                    //오늘 보낸 것이랑 어제 보낸 것의 날짜가 다르면 경계선 그어주고 오늘 날짜 보여주기
                    meesageViewholder.receilinear.setVisibility(View.VISIBLE);
                    meesageViewholder.receiymdtx.setText(messageslist.get(i).getYmd());
                } else if (!messageslist.get(i).getYmd().equals(messageslist.get(i - 1).getYmd())) {
                    meesageViewholder.receilinear.setVisibility(View.VISIBLE);
                    meesageViewholder.receiymdtx.setText(messageslist.get(i).getYmd());
                } else {
                    meesageViewholder.receilinear.setVisibility(View.GONE);
                }

                meesageViewholder.gooutlinear.setVisibility(View.VISIBLE);
                String join = messageslist.get(i).getSendername() + "님이 " + messageslist.get(i).getType() + "님(들)을 채팅방으로 초대했습니다. ";
                meesageViewholder.goouttx.setText(join);
                Log.d(TAG, "onBindViewHolder: " + join);

                //이미지랑 이미지 시간도도
                meesageViewholder.receimgv.setVisibility(View.GONE);
                meesageViewholder.receimgtimetx.setVisibility(View.GONE);
                //프로필 사진,이름이랑 메세지 안보이게
                meesageViewholder.renametx.setVisibility(View.INVISIBLE);
                meesageViewholder.remessagetx.setVisibility(View.INVISIBLE);
                meesageViewholder.recirimg.setVisibility(View.GONE);


            } else {
                meesageViewholder.gooutlinear.setVisibility(View.GONE);


                Log.d(TAG, "onBindViewHolder: " + messageslist.get(i).getImgurl());

                //이름 구분
                if (i == 0) {
                    //처음 메세지는 이름 보여줌
                    meesageViewholder.renametx.setVisibility(View.VISIBLE);
                    meesageViewholder.renametx.setText(messageslist.get(i).getSendername());
                } else if (!messageslist.get(i).getSendername().equals(messageslist.get(i - 1).getSendername())) {
                    //전에 보낸 사람이랑 이름이 다르면 보여주고 이름 넣어준다..
                    meesageViewholder.renametx.setVisibility(View.VISIBLE);
                    meesageViewholder.renametx.setText(messageslist.get(i).getSendername());
                } else {
                    //전에 보낸 메세지가 내꺼인 경우 전 메세지와 시간이 다르면 보여준다.
                    if (!messageslist.get(i).getHm().equals(messageslist.get(i - 1).getHm()) || messageslist.get(i - 1).getMessage().equals("^___join___^")) {
                        meesageViewholder.renametx.setVisibility(View.VISIBLE);
                        meesageViewholder.renametx.setText(messageslist.get(i).getSendername());
                    } else {
                        Log.d(TAG, "onBindViewHolder:전메시지가 내꺼이고 전 메세지와 시간이 다를경우 이름 사진 안보여줘 ");
//                        meesageViewholder.renametx.setVisibility(View.GONE);
//                        meesageViewholder.recirimg.setVisibility(View.GONE);
                    }
                }

                //사진 구분
                if (i == 0) {
                    //처음에는 상대방 사진을 넣어준다.

                    meesageViewholder.recirimg.setVisibility(View.VISIBLE);
                    Glide.with(mctx).load(messageslist.get(i).getImgurl()).into(meesageViewholder.recirimg);
                } else if (!messageslist.get(i).getImgurl().equals(messageslist.get(i - 1).getImgurl())) {
                    //전 메세지랑 이미지가 다르고 보낸 사람이 내가 아니라면 사진 넣어준다.
                    meesageViewholder.recirimg.setVisibility(View.VISIBLE);
                    Glide.with(mctx).load(messageslist.get(i).getImgurl()).into(meesageViewholder.recirimg);
                } else {
                    //전 메세지랑 이미지가 다르고, 보낸사람이 내가 아니고, 년월일이 다르고, 시분이 달라지면 사진을 넣어준다.
                    if (!messageslist.get(i).getHm().equals(messageslist.get(i - 1).getHm()) || messageslist.get(i - 1).getMessage().equals("^___join___^")) {
                        meesageViewholder.recirimg.setVisibility(View.VISIBLE);
                        Glide.with(mctx).load(messageslist.get(i).getImgurl()).into(meesageViewholder.recirimg);
                    } else {
                        meesageViewholder.renametx.setVisibility(View.GONE);
                        meesageViewholder.recirimg.setVisibility(View.GONE);
                    }


                }
                Log.d(TAG, "onBindViewHolder: i and size " + i + "\n" + messageslist.size());
                //시간 구분
                if (i == messageslist.size() - 1) {
                    //마지막 메세지의 시간인 경우
                    //시간을 넣어준다.
                    meesageViewholder.retimetx.setVisibility(View.VISIBLE);
                    meesageViewholder.retimetx.setText(messageslist.get(i).getHm());
                } else if (messageslist.get(i).getHm().equals(messageslist.get(i + 1).getHm()) && messageslist.get(i).getSendername().equals(messageslist.get(i + 1).getSendername())) {
                    //새로운 메세지와 다음 메세지가 시분이 같다면 새로운 메세지의 시간을 안보이게 한다.
                    meesageViewholder.retimetx.setVisibility(View.GONE);
                } else {
                    //시분이 다른 경우 새로운 메세지의 시분을 보이게 한다.
                    meesageViewholder.retimetx.setVisibility(View.VISIBLE);
                    meesageViewholder.retimetx.setText(messageslist.get(i).getHm());
                }

                //날짜 구분
                if (i == 0) {
                    //메세지가 하나이거나
                    //오늘 보낸 것이랑 어제 보낸 것의 날짜가 다르면 경계선 그어주고 오늘 날짜 보여주기
                    meesageViewholder.receilinear.setVisibility(View.VISIBLE);
                    meesageViewholder.receiymdtx.setText(messageslist.get(i).getYmd());
                } else if (!messageslist.get(i).getYmd().equals(messageslist.get(i - 1).getYmd())) {
                    meesageViewholder.receilinear.setVisibility(View.VISIBLE);
                    meesageViewholder.receiymdtx.setText(messageslist.get(i).getYmd());
                } else {
                    meesageViewholder.receilinear.setVisibility(View.GONE);

                }

                //안나갔다면
                Log.d(TAG, "onBindViewHolder: message " + messageslist.get(i).getMessage());
                Log.d(TAG, "onBindViewHolder: type: " + messageslist.get(i).getType());
                //메세지가 이미지인지 텍스트인지 구분
                if (messageslist.get(i).getType().equals("mtext")) {
                    //텍스트인 경우
                    meesageViewholder.receimgtimetx.setVisibility(View.GONE);
                    meesageViewholder.receimgv.setVisibility(View.GONE);

                    Log.d(TAG, "onBindViewHolder: time " + messageslist.get(i).getHm());
                    Log.d(TAG, "onBindViewHolder: name" + messageslist.get(i).getSendername());
                    meesageViewholder.remessagetx.setVisibility(View.VISIBLE);
                    meesageViewholder.remessagetx.setText(messageslist.get(i).getMessage());
                } else {
                    Log.d(TAG, "onBindViewHolder: image");
                    //이미지인 경우 텍스트 넣는 곳이랑 시간 안보이게
                    meesageViewholder.remessagetx.setVisibility(View.GONE);
                    meesageViewholder.retimetx.setVisibility(View.GONE);
                    //이미지 레이아웃 보이게
                    meesageViewholder.receimgv.setVisibility(View.VISIBLE);
                    meesageViewholder.receimgtimetx.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onBindViewHolder: othersimgtime: " + messageslist.get(i).getHm());
                    Log.d(TAG, "onBindViewHolder: othersimg: " + messageslist.get(i).getMessage());
                    Log.d(TAG, "onBindViewHolder: otherimg" + messageslist.get(i).getBitmaptoString());
                    if (messageslist.get(i).getBitmaptoString() != null) {
                        //방금 메세지를 받은 경우
                        byte[] decodedString = Base64.decode(messageslist.get(i).getBitmaptoString(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        Glide.with(mctx).load(decodedByte).into(meesageViewholder.receimgv);
                    } else {
                        Log.d(TAG, "onBindViewHolder:url " + messageslist.get(i).getMessage());
                        //저장된 이미지 불러오기
                        Glide.with(mctx).load(messageslist.get(i).getMessage()).centerCrop().thumbnail(0.6f).into(meesageViewholder.receimgv);
                    }
                   /* //이미지를 클릭했을 때 전체 이미지를 볼 수 있음
                    meesageViewholder.receimgv.setOnClickListener(v -> {
                        //이미지가 있는 MESSAGE 모으기
                        ArrayList<Message> messagesli = new ArrayList<>();
                        for (int img = 0; img < messageslist.size(); img++) {
                            if (messageslist.get(img).getType().equals("image")) {
                                messagesli.add(messageslist.get(img));
                            }
                        }
                        Log.d(TAG, "onBindViewHolder:imglist size "+messagesli.size());
*//*

                        if (messagesli.get(i).getBitmaptoString() == null){
                             //이미지 url이 있는 경우
                        }
                        //모은 message중에서 사용자가 클릭한 이미지가 새로운 리스트에서 몇 번째에 있는지 찾기
                        for (int posi = 0; posi < messagesli.size(); posi++) {
                            if (messagesli.get(posi).get.equals(messageslist.get(i).getMessage())) {
                                clickposi = posi;
                                break;
                            }
                        }
*//*

                        //전달
                        Intent intent = new Intent(mctx, PictureActivity.class);
                        intent.putExtra("clickposition", clickposi);
                        intent.putExtra("rnum",messageslist.get(i).getRnum());
                        mctx.startActivity(intent);

                    });*/
                    //시간 구분
                    if (i == messageslist.size() - 1) {
                        //마지막 메세지의 시간인 경우
                        //시간을 넣어준다.
                        meesageViewholder.receimgtimetx.setVisibility(View.VISIBLE);
                        meesageViewholder.receimgtimetx.setText(messageslist.get(i).getHm());
                    } else if (messageslist.get(i).getHm().equals(messageslist.get(i + 1).getHm()) && messageslist.get(i).getSendername().equals(messageslist.get(i + 1).getSendername())) {
                        //새로운 메세지와 다음 메세지가 시분이 같다면 새로운 메세지의 시간을 안보이게 한다.
                        meesageViewholder.receimgtimetx.setVisibility(View.GONE);
                    } else {
                        //시분이 다른 경우 새로운 메세지의 시분을 보이게 한다.
                        meesageViewholder.receimgtimetx.setVisibility(View.VISIBLE);
                        meesageViewholder.receimgtimetx.setText(messageslist.get(i).getHm());
                    }
//                    meesageViewholder.receimgv.setImageBitmap(messageslist.get(i).getBitmaptoString());

//                    Glide.with(mctx).load(messageslist.get(i).getMessage()).thumbnail(0.25f).centerCrop().placeholder(R.drawable.imageload).into(meesageViewholder.receimgv);

                }
            }
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + messageslist.size());
        return messageslist.size();
    }

    public class MeesageViewholder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView semessagetx, stimetx;
        TextView remessagetx, retimetx, renametx;
        CircleImageView recirimg;
        LinearLayout sendlinear, receilinear, gooutlinear, invitelinear;
        TextView sendymdTx, receiymdtx, goouttx, sendimgtimetx, receimgtimetx, invitetx;
        ImageView sendimgv, receimgv;

        public MeesageViewholder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "MeesageViewholder:viewtype " + viewtype);
            if (viewtype == MSG_RIGHT) {
                //내가 보낸 것이라면
                sendlinear = itemView.findViewById(R.id.linear_message_sender);
                sendymdTx = itemView.findViewById(R.id.ymd_message_sender);
                semessagetx = itemView.findViewById(R.id.message_sender);
                stimetx = itemView.findViewById(R.id.time_sender);

                //초대 했다는 것을 알림
                invitelinear = itemView.findViewById(R.id.invite_linear_sender);
                invitetx = itemView.findViewById(R.id.invite_tx_sender);

                sendimgv = itemView.findViewById(R.id.img_sender);
                sendimgtimetx = itemView.findViewById(R.id.img_time_sender);
            } else {
                //상대방이 보낸 것이라면
                receilinear = itemView.findViewById(R.id.linear_message_received);
                receiymdtx = itemView.findViewById(R.id.ymd_message_received);
                recirimg = itemView.findViewById(R.id.cirimg_receiver);
                remessagetx = itemView.findViewById(R.id.message_receiver);
                retimetx = itemView.findViewById(R.id.time_receiver);
                renametx = itemView.findViewById(R.id.name_tx_receiver);

                goouttx = itemView.findViewById(R.id.goout_tx_message);
                gooutlinear = itemView.findViewById(R.id.goout_linear_message);

                receimgv = itemView.findViewById(R.id.img_receiver);
                receimgtimetx = itemView.findViewById(R.id.img_time_receiver);
            }
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            itemlistener.recyclerViewListClicked(v, this.getLayoutPosition());
        }
    }
}
