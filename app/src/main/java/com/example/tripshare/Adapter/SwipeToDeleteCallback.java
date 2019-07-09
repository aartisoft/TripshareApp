package com.example.tripshare.Adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.example.tripshare.R;

import static com.example.tripshare.Trip.ItineraryActivity.dialogcancel;

public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {
    private static final String TAG = "RecyclerCallback";
    Context mContext;
    private Paint mClearPaint;
    private ColorDrawable mBackground;
    private int backgroundColor;
    private Drawable deleteDrawable;
    private int intrinsicWidth;
    private int intrinsicHeight;
    private final ItemTouchHelperContract itemTouchHelperContract;




    public SwipeToDeleteCallback(Context context, ItemTouchHelperContract mAdapter) {
        mContext = context;
        this.itemTouchHelperContract = mAdapter;
        mBackground = new ColorDrawable();
        backgroundColor = Color.parseColor("#b80f0a");
        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

           }


    @Override //Here we set the direction of swipe.
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG, "getMovementFlags: ");
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, ItemTouchHelper.LEFT);
    }

    @Override //This is used for drag and drop
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        Log.d(TAG, "onMove: ");
        Log.d(TAG, "onMove: ");
        itemTouchHelperContract.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override //스와이프 발생하는 뷰
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        Log.d(TAG, "onChildDraw: ");
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        boolean isCancelled = dX == 0 && !isCurrentlyActive;
        Log.d(TAG, "onChildDraw: iscancelled "+isCancelled);
        Log.d(TAG, "onChildDraw: dialog"+dialogcancel);
        if (isCancelled) {
            Log.d(TAG, "onChildDraw: isCancelled");
            clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }
        Log.d(TAG, "onChildDraw: not cancelled");
        //스와이프 될 때 배경색
        mBackground.setColor(backgroundColor);
        mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        mBackground.draw(c);

        deleteDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_delete_black_24dp);
        intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        intrinsicHeight = deleteDrawable.getIntrinsicHeight();

        int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
        int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
        int deleteIconRight = itemView.getRight() - deleteIconMargin;
        int deleteIconBottom = deleteIconTop + intrinsicHeight;

        Log.d(TAG, "onChildDraw: setBounds ");
        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
        deleteDrawable.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);


    }
    //스와이프 행동이 끝났을 경우
    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        Log.d(TAG, "clearCanvas: ");
        c.drawRect(left, top, right, bottom, mClearPaint);
//       if (dialogcancel.equals("yes cancel")){
//           c.restore();
//       }

    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG, "getSwipeThreshold: 50퍼센트 이상 스와이퍼 되서 그대로 때면 지워질거야. 그 때 onswipe가 발생함 ");
        return 0.5f;
    }

    @Override //스와이프 할 경우
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        Log.d(TAG, "onSwiped: 스와이프 동작이 감지될 때 ");
        Log.d(TAG, "onSwiped: int "+i);

    }

    public interface ItemTouchHelperContract {

        void onRowMoved(int fromPosition, int toPosition);
        void onRowSelected(ItineraryRCVAdapter.MyViewHolder myViewHolder);
        void onRowClear(ItineraryRCVAdapter.MyViewHolder myViewHolder);

    }

    @Override
    public boolean isLongPressDragEnabled() {
        Log.d(TAG, "isLongPressDragEnabled: ");
        return false;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        Log.d(TAG, "onSelectedChanged: ");

        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof ItineraryRCVAdapter.MyViewHolder) {
                ItineraryRCVAdapter.MyViewHolder myViewHolder=
                        (ItineraryRCVAdapter.MyViewHolder) viewHolder;
                itemTouchHelperContract.onRowSelected(myViewHolder);
            }

        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        Log.d(TAG, "clearView: ");

        if (viewHolder instanceof ItineraryRCVAdapter.MyViewHolder) {
            ItineraryRCVAdapter.MyViewHolder myViewHolder=
                    (ItineraryRCVAdapter.MyViewHolder) viewHolder;
            itemTouchHelperContract.onRowClear(myViewHolder);
        }
    }
}