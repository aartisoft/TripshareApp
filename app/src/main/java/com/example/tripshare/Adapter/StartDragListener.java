package com.example.tripshare.Adapter;

import android.support.v7.widget.RecyclerView;

public interface StartDragListener {
    void requestDrag(RecyclerView.ViewHolder viewHolder);
    void afterDrag();
}
