package com.example.tripshare.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tripshare.Data.Room;
import com.example.tripshare.R;
import com.example.tripshare.TripTalk.MessageActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.RoomViewHolder> implements Filterable {

    //현재 시간과 비교해서 오늘이면 시간
    //오늘이 아니면 날짜 보여주기!

    private Context mtx;
    private ArrayList<Room> roomlist, roomfulllist;
    private static final String TAG = "ChatRoomAdapter";
    String[] imgs;
    public ChatRoomAdapter(Context context, ArrayList<Room> roomArrayList) {
        this.mtx = context;
        this.roomlist = roomArrayList;
        roomfulllist = new ArrayList<>(roomlist);

    }

    @NonNull
    @Override
    public ChatRoomAdapter.RoomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(mtx);
        View view = layoutInflater.inflate(R.layout.itemchatroom, viewGroup, false);

        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomAdapter.RoomViewHolder roomViewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: " + roomlist.get(i).getYourname());

        //아무도 없는 경우 알 수 없음 해줘
        roomViewHolder.yournametx.setText(roomlist.get(i).getYourname());

        Log.d(TAG, "onBindViewHolder:room " + roomlist.get(i).getTotal());
        Log.d(TAG, "onBindViewHolder: " + roomlist.get(i).getRnum());
        int total = Integer.valueOf(roomlist.get(i).getTotal()) + 1;
        String stotal = String.valueOf(total);

        //사진이라면 사진 전송했다고 보여주기
        if (roomlist.get(i).getType().equals("mtext")) {
            //텍스트인 경우 메세지 그대로 보여줌
            //하지만 나간 경우에는 안 보여준다.

            if (roomlist.get(i).getLastmessage().equals("^___goout___^")){
                roomViewHolder.lastmessagetx.setText(" ");
            }else {
                roomViewHolder.lastmessagetx.setText(roomlist.get(i).getLastmessage());
            }
        } else if (roomlist.get(i).getType().equals("image")) {
            //사진인 경우 url대신 사진 보냈다고 보여줘
            roomViewHolder.lastmessagetx.setText("이미지를 전송했습니다.");
        } else {
            //채팅방을 만들고 아무말도 안한 경우
            roomViewHolder.lastmessagetx.setText(" ");
            roomViewHolder.datetimetx.setText(" ");
        }

        //아무도 채팅방에 없는 경우
        if (roomlist.get(i).getTotal().equals("0")) {
            roomViewHolder.yournametx.setText("알 수 없음");
            roomViewHolder.lastmessagetx.setText(" ");
        }

        //3명 이상인 방만 사람 수 보여주기
        if (total > 2) {
            roomViewHolder.totaltx.setVisibility(View.VISIBLE);
            roomViewHolder.totaltx.setText(stotal);
        } else {
            roomViewHolder.totaltx.setVisibility(View.INVISIBLE);
        }

        if (total >=3){

            //방 구성원이 여려명인 경우 이미지를 나눈다.
            imgs = roomlist.get(i).getYourimgurl().split(",");

        }
        //방 구성원이 2명,3명,4명,5명 이상인 경우
        if (total >= 5) {

            roomViewHolder.fourcon.setVisibility(View.VISIBLE);
            roomViewHolder.threecon.setVisibility(View.INVISIBLE);
            roomViewHolder.twocon.setVisibility(View.INVISIBLE);
            roomViewHolder.yourimgcir.setVisibility(View.INVISIBLE);
            //4명의 사진
            Glide.with(mtx).load(imgs[0]).into(roomViewHolder.fourfcir);
            Glide.with(mtx).load(imgs[1]).into(roomViewHolder.fourscir);
            Glide.with(mtx).load(imgs[2]).into(roomViewHolder.fourtcir);
            Glide.with(mtx).load(imgs[3]).into(roomViewHolder.fourfourthcir);
        } else if (total == 4) {

            roomViewHolder.fourcon.setVisibility(View.INVISIBLE);
            roomViewHolder.threecon.setVisibility(View.VISIBLE);
            roomViewHolder.twocon.setVisibility(View.INVISIBLE);
            roomViewHolder.yourimgcir.setVisibility(View.INVISIBLE);
            //3명 사진
            Glide.with(mtx).load(imgs[0]).into(roomViewHolder.threefimgcir);
            Glide.with(mtx).load(imgs[1]).into(roomViewHolder.threesimgcir);
            Glide.with(mtx).load(imgs[2]).into(roomViewHolder.threetimgcir);

        } else if (total == 3) {

            roomViewHolder.fourcon.setVisibility(View.INVISIBLE);
            roomViewHolder.threecon.setVisibility(View.INVISIBLE);
            roomViewHolder.twocon.setVisibility(View.VISIBLE);
            roomViewHolder.yourimgcir.setVisibility(View.INVISIBLE);
            //2명의 사진 보여줘
            Glide.with(mtx).load(imgs[0]).into(roomViewHolder.twofimgcir);
            Glide.with(mtx).load(imgs[1]).into(roomViewHolder.twosimgcir);
        } else if (total == 2) {

            roomViewHolder.fourcon.setVisibility(View.INVISIBLE);
            roomViewHolder.threecon.setVisibility(View.INVISIBLE);
            roomViewHolder.twocon.setVisibility(View.INVISIBLE);
            roomViewHolder.yourimgcir.setVisibility(View.VISIBLE);
            //1명의 사진 보여줘
            Glide.with(mtx).load(roomlist.get(i).getYourimgurl()).into(roomViewHolder.yourimgcir);
            Log.d(TAG, "onBindViewHolder: " + roomlist.get(i).getYourimgurl());
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy년 M월 d일 E");
        Date current = new Date();
        String today = df.format(current);
        Log.d(TAG, "onBindViewHolder: today " + today);
        Log.d(TAG, "onBindViewHolder: room " + roomlist.get(i).getYmd());
        Log.d(TAG, "onBindViewHolder: rnum" + roomlist.get(i).getRnum());
        //가장 최근 메세지 시간 보여주기
        if (today.equals(roomlist.get(i).getYmd())) {

            roomViewHolder.datetimetx.setText(roomlist.get(i).getHm());
        } else {
            if (roomlist.get(i).getYmd().length() > 5) {
                String subdate = roomlist.get(i).getYmd().substring(2, roomlist.get(i).getYmd().length() - 2);
                Log.d(TAG, "onBindViewHolder: sub" + subdate);
                roomViewHolder.datetimetx.setText(subdate);
            }

        }
        roomViewHolder.layout.setOnClickListener(v -> {
            //1:1채팅방,다중 채팅방으로 가!
            Intent moveintent = new Intent(mtx, MessageActivity.class);
            moveintent.putExtra("name", roomlist.get(i).getYourname());
            moveintent.putExtra("email", roomlist.get(i).getYouremail());
            moveintent.putExtra("rnum", roomlist.get(i).getRnum());
            moveintent.putExtra("total", roomlist.get(i).getTotal());
            moveintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mtx.startActivity(moveintent);
        });
    }


    @Override
    public int getItemCount() {
        return roomlist.size();
    }

    @Override
    public Filter getFilter() {
        return roomfilter;
    }

    private Filter roomfilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Room> filterdlist = new ArrayList<>();

            //사용자가 입력한 이름이 속한 채팅방을 가진 리스트를 새로 만듬
            //사용자가 입력한 문자가 아무것도 없다면 다 보여줌
            if (constraint == null || constraint.length() == 0) {
                filterdlist.addAll(roomfulllist);
            } else {
                //문자가 있다면 문자열로 바꾼다.
                String filterpattern = constraint.toString().toLowerCase().trim();

                for (Room room : roomfulllist) {
                    //사용자가 입력한 이름이 등록된 사용자의 이름에 있는 경우에 리스트에 추가하는 것
                    if (room.getYourname().toLowerCase().contains(filterpattern)) {
                        filterdlist.add(room);
                    }
                }
            }

            //검색 결과와 일치한 리스트가진 필터링 결과를 반환
            FilterResults results = new FilterResults();
            results.values = filterdlist;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            roomlist.clear();
            roomlist.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class RoomViewHolder extends RecyclerView.ViewHolder {

        CircleImageView yourimgcir, twofimgcir, twosimgcir, threefimgcir, threesimgcir, threetimgcir;
        CircleImageView fourfcir, fourscir, fourtcir, fourfourthcir;

        TextView yournametx, lastmessagetx, datetimetx, totaltx;
        ConstraintLayout layout, twocon, threecon, fourcon;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            twocon = itemView.findViewById(R.id.two_conlayout_chatroom);
            threecon = itemView.findViewById(R.id.three_conlayout_chatroom);
            fourcon = itemView.findViewById(R.id.four_conlayout);

            twofimgcir = itemView.findViewById(R.id.twofristcirimg_chatroom);
            twosimgcir = itemView.findViewById(R.id.two_secondcirimg_chatroom);

            threefimgcir = itemView.findViewById(R.id.threefristcirimg_chatroom);
            threesimgcir = itemView.findViewById(R.id.threesecondcirimg_chatroom);
            threetimgcir = itemView.findViewById(R.id.threethirdcirimg_chatroom);

            fourfcir = itemView.findViewById(R.id.four_fristcirimg_chatroom);
            fourscir = itemView.findViewById(R.id.four_second_cirimg_chatroom);
            fourtcir = itemView.findViewById(R.id.four_third_cirimg_chatroom);
            fourfourthcir = itemView.findViewById(R.id.four_fourth_cirimg_chatroom);

            yourimgcir = itemView.findViewById(R.id.cirimg_chatroom);
            yournametx = itemView.findViewById(R.id.name_chatroom);
            lastmessagetx = itemView.findViewById(R.id.lastmessage_chatroom);
            layout = itemView.findViewById(R.id.con_layout_chatroom);
            datetimetx = itemView.findViewById(R.id.datetime_tx_Chatroom);
            totaltx = itemView.findViewById(R.id.total_tx_Chatroom);
        }
    }
}
