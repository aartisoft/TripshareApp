package com.example.tripshare.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.tripshare.Data.Message;
import com.example.tripshare.R;

import java.util.ArrayList;

public class PictureAdapter extends PagerAdapter {
    Context mtx;
    private ArrayList<Message> messagelist;
    private static final String TAG = "PictureAdapter";

    public PictureAdapter(Context mtx, ArrayList<Message> messagelist) {
        this.mtx = mtx;
        this.messagelist = messagelist;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) mtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.pictureimage, container, false);

        ImageView imageView = view.findViewById(R.id.image_picture);

        Log.d(TAG, "instantiateItem: " + messagelist.get(position).getMessage());
        if (!messagelist.get(position).getMessage().equals("no")) {
            Log.d(TAG, "instantiateItem: ");
            Glide.with(mtx).load(messagelist.get(position).getMessage()).thumbnail(0.5f).into(imageView);

        } else {
            byte[] decodedString = Base64.decode(messagelist.get(position).getBitmaptoString(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Glide.with(mtx).load(decodedByte).into(imageView);
            Log.d(TAG, "instantiateItem:bitmap " + messagelist.get(position).getBitmaptoString());
        }
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }

    @Override
    public int getCount() {
        return messagelist.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view == (LinearLayout) o);
    }
}
