package com.example.tripshare.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tripshare.Data.Message;
import com.example.tripshare.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class StreamChatAdapter extends RecyclerView.Adapter<StreamChatAdapter.StreamViewholder> {
    Context mtx;
    ArrayList<Message> messageArrayList;

    public StreamChatAdapter(Context mtx, ArrayList<Message> messageArrayList) {
        this.mtx = mtx;
        this.messageArrayList = messageArrayList;
    }

    @NonNull
    @Override
    public StreamViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(mtx);
        View view = layoutInflater.inflate(R.layout.streamchat, viewGroup, false);
        return new StreamViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StreamViewholder streamViewholder, int i) {

        if (messageArrayList.get(i).getType().equals("token")){
            //토큰 텍스트 보이고 입력해주기
            streamViewholder.tokentx.setVisibility(View.VISIBLE);
            //텍스트,이름,프사 안 보이게 하기
            streamViewholder.circleImageView.setVisibility(View.GONE);
            streamViewholder.messagetx.setVisibility(View.GONE);
            streamViewholder.nametx.setVisibility(View.GONE);
            String tokenmessage = messageArrayList.get(i).getSendername() + "님이 "+messageArrayList.get(i).getMessage() +"개의 토큰을 선물했습니다.";

            streamViewholder.tokentx.setText(tokenmessage);
        }else {
            //토큰 입력창은 안 보이게
            streamViewholder.tokentx.setVisibility(View.GONE);
            //텍스트,이름,프사 보이게하기
            streamViewholder.circleImageView.setVisibility(View.VISIBLE);
            streamViewholder.messagetx.setVisibility(View.VISIBLE);
            streamViewholder.nametx.setVisibility(View.VISIBLE);

            //텍스트랑 프로필 사진 입력하기
            streamViewholder.messagetx.setText(messageArrayList.get(i).getMessage());
            streamViewholder.nametx.setText(messageArrayList.get(i).getSendername());
            Glide.with(mtx).load(messageArrayList.get(i).getImgurl()).thumbnail(0.5f).into(streamViewholder.circleImageView);
        }
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public class StreamViewholder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView nametx, messagetx,tokentx;

        public StreamViewholder(@NonNull View itemView) {
            super(itemView);
            tokentx = itemView.findViewById(R.id.token_receiver);
            circleImageView = itemView.findViewById(R.id.cirimg_stream);
            nametx = itemView.findViewById(R.id.name_tx_stream);
            messagetx = itemView.findViewById(R.id.message_tx_stream);
        }
    }
}
