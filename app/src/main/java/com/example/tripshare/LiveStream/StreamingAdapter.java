package com.example.tripshare.LiveStream;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tripshare.R;
import com.example.tripshare.TripTalk.RecyclerViewClickListener;

import java.util.ArrayList;

public class StreamingAdapter extends RecyclerView.Adapter<StreamingAdapter.RoomViewHolder> {
    Context ctx;
    ArrayList<Livestream> streamList;
    private static RecyclerViewClickListener itemlistener;

    public StreamingAdapter(Context context, ArrayList<Livestream> livestreamList, RecyclerViewClickListener itemlistener) {
        this.ctx = context;
        this.streamList = livestreamList;
        this.itemlistener = itemlistener;

    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(ctx);
        View view = layoutInflater.inflate(R.layout.example_row, viewGroup, false);

        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder roomViewHolder, int i) {


        String roomname = "방 제목 : " + streamList.get(i).getName();
        String name = "이름 : " + streamList.get(i).getSourceConnectionInformation().getUsername();
        roomViewHolder.roomnametexV.setText(roomname);
        roomViewHolder.usernametexV.setText(name);

        if (streamList.get(i).getThumbnail_url() != null) {
            //이미지
            if (!streamList.get(i).getThumbnail_url().equals("no image")) {
                Glide.with(ctx).load(streamList.get(i).getThumbnail_url()).thumbnail(0.3f).centerCrop()
                        .into(roomViewHolder.roomimgV);
            }
        }
    }

    @Override
    public int getItemCount() {
        return streamList.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView roomimgV;
        TextView usernametexV, roomnametexV;
        LinearLayout linearLayout;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomimgV = itemView.findViewById(R.id.roomimg_stream);
            usernametexV = itemView.findViewById(R.id.username_tx_streaming);
            roomnametexV = itemView.findViewById(R.id.roomname_tx_streaming);
            linearLayout = itemView.findViewById(R.id.linear_stream);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemlistener.recyclerViewListClicked(v, this.getLayoutPosition());
        }
    }
}
