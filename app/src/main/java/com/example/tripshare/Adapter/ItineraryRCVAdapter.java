package com.example.tripshare.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.OnedayPlace;
import com.example.tripshare.R;
import com.example.tripshare.Trip.DirectionActivity;
import com.example.tripshare.Trip.ItineraryActivity;
import com.example.tripshare.WhereWhen.LocationAutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteActivity;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItineraryRCVAdapter extends RecyclerView.Adapter<ItineraryRCVAdapter.MyViewHolder>  implements SwipeToDeleteCallback.ItemTouchHelperContract{
    private static final String TAG = "RecyclerRCVAdapter";
    private ArrayList<OnedayPlace> placeListArrayList;
    private final StartDragListener mStartDragListener;
    private int numorder, startnum, startporder, endporder;
    public static ApiInterface apiInterface;
    private Context mcontext;
    private Activity mactivity;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "RecyclerMyViewHolder";
        private TextView mTitle,mNum, directiontxt;
        RelativeLayout relativeLayout;
        ImageView imageView;
        View rowView;

        public MyViewHolder(View itemView) {
            super(itemView);
            rowView = itemView;
            Log.d(TAG, "MyViewHolder: ");
            mNum = itemView.findViewById(R.id.num_cardview_itinerary);
            mTitle = itemView.findViewById(R.id.place_cardview_itinerary);
            imageView = itemView.findViewById(R.id.updown_itinerary);
            directiontxt = itemView.findViewById(R.id.direction_txt_itinerary);

        }
    }

    public ItineraryRCVAdapter(ArrayList<OnedayPlace> placeListArrayList, StartDragListener mStartDragListener, Context context) {
        this.mStartDragListener = mStartDragListener;
        Log.d(TAG, "ItineraryRCVAdapter:placeListArrayList ");
        this.placeListArrayList = placeListArrayList;
        this.mcontext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mcontext).inflate(R.layout.cardview_row, parent, false);
        Log.d(TAG, "onCreateViewHolder: ");

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: "+placeListArrayList.get(position).getPlacename());
        String num = String.valueOf(position+1);
        holder.mNum.setTextSize(20);
        holder.mTitle.setTextSize(15);

        holder.mNum.setText(num);
        holder.mTitle.setText(placeListArrayList.get(position).getPlacename());
        Log.d(TAG, "onBindViewHolder: position"+position);
        holder.imageView.setOnTouchListener((v, event) -> {
            //장소 순서 변경을 클릭했을 때의 아이템 번호와 식별자
            startnum = position;
            numorder = position;
            endporder = placeListArrayList.get(position).getPorder();
            Log.d(TAG, "imageview onTouch: position and porder >>"+position+"\n"+placeListArrayList.get(position).getPorder());
            if (event.getAction() ==
                    MotionEvent.ACTION_DOWN) {
                mStartDragListener.requestDrag(holder);
            }
            return false;
        });

        holder.directiontxt.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder:길찾기 클릭 ");

            if (position ==0){
                //처음 장소를 클릭했을 경우 다이어로그를 통해서 출발지를 직접 입력할 것인지, 내 위치로 할 것인지 선택

//                Log.d(TAG, "onBindViewHolder:처음 장소 클릭");
                Intent searchintent = new Intent(mcontext, LocationAutocompleteActivity.class);
                searchintent.putExtra("dslat",placeListArrayList.get(position).getLatitude());
                searchintent.putExtra("dslong",placeListArrayList.get(position).getLongitude());
                searchintent.putExtra("dsname",placeListArrayList.get(position).getPlacename());
                searchintent.putExtra("일정","길찾기");
                mcontext.startActivity(searchintent);
            }else {
                Log.d(TAG, "onBindViewHolder:두 번째 순서 이상 장소클릭했을 경우 ");
                Intent dirintent = new Intent(mcontext, DirectionActivity.class);
                dirintent.putExtra("orlat",placeListArrayList.get(position-1).getLatitude());
                dirintent.putExtra("orlong",placeListArrayList.get(position-1).getLongitude());
                dirintent.putExtra("dslat",placeListArrayList.get(position).getLatitude());
                dirintent.putExtra("dslong",placeListArrayList.get(position).getLongitude());
                dirintent.putExtra("orname",placeListArrayList.get(position-1).getPlacename());
                dirintent.putExtra("dsname",placeListArrayList.get(position).getPlacename());
                mcontext.startActivity(dirintent);
            }

        });

    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: placeListArrayList.size() "+placeListArrayList.size());
        return placeListArrayList.size();
    }

        //여기서 서버랑 통신해서 서버에 있는 데이터도 지우면 될 듯
    public void removeItem(int position) {
        Log.d(TAG, "removeItem: position "+position);
        placeListArrayList.remove(position);
        notifyItemRemoved(position);


    }

    public void restoreItem(String item, int position) {
        Log.d(TAG, "restoreItem: restoreItem "+position);
        placeListArrayList.add(position, placeListArrayList.get(position));
        notifyItemInserted(position);
    }

    public ArrayList<OnedayPlace> getData() {
        Log.d(TAG, "getData: Data "+placeListArrayList);
        return placeListArrayList;
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        Log.d(TAG, "onRowMoved: from: "+fromPosition);
        Log.d(TAG, "onRowMoved: to: "+toPosition);
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(placeListArrayList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(placeListArrayList, i, i - 1);
            }
        }
        numorder = toPosition;
        notifyItemMoved(fromPosition, toPosition);

        //실행되고 getitemcount 실행됨
    }


    @Override
    public void onRowSelected(MyViewHolder myViewHolder) {
        Log.d(TAG, "onRowSelected: ");
        myViewHolder.rowView.setBackgroundColor(Color.GRAY);

    }

    @Override
    public void onRowClear(MyViewHolder myViewHolder) {
        //처음 아이템이 이동한  때의 아이템 번호 numorder
        Log.d(TAG, "onRowClear: start "+startnum);
        Log.d(TAG, "onRowClear: numorder "+ numorder);
//        Log.d(TAG, "onRowClear:startporder "+placeListArrayList.get(startnum).getPorder());
//        Log.d(TAG, "onRowClear:endporder "+placeListArrayList.get(numorder).getPorder());
//        Log.d(TAG, "onRowClear: placeListArrayList "+ placeListArrayList);

        if (startnum != numorder){
            //드래그 앤 드롭 했을 때 다른 위치로 이동한 경우
            String porderchanged = "";
            //변경된 이후의 장소들의 고유번호를 하나의 문자열로 만든다.
            for (int i =0; i <placeListArrayList.size() ; i++){
                Log.d(TAG, "onRowClear: arraylist porder: "+placeListArrayList.get(i).getPorder());
                porderchanged = porderchanged+placeListArrayList.get(i).getPorder()+".";
            }
            //문자열에서 마지막 .을 뺀다
            String lastporder = porderchanged.substring(0,porderchanged.length()-1);
            //장소들의 개수
            int placesize = placeListArrayList.size();
            Log.d(TAG, "onRowClear: 장소 갯수"+placesize);
            Log.d(TAG, "onRowClear: 서버로 보낼 문자열 "+lastporder);

            //장소들의 고유번호와 장소의 개수를 서버에 보내서 장소들의 순서를 변경해준다.
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            apiInterface.Editmyplace(lastporder, placesize).enqueue(new Callback<OnedayPlace>() {
                @Override
                public void onResponse(Call<OnedayPlace> call, Response<OnedayPlace> response) {
                    if (response.isSuccessful()){
                        notifyDataSetChanged();
                        mStartDragListener.afterDrag();
                        Log.d(TAG, "onResponse: "+response.body().getResponse());
                    }else {
                        Log.d(TAG, "onResponse: error");
                    }
                }

                @Override
                public void onFailure(Call<OnedayPlace> call, Throwable t) {
                    Log.d(TAG, "onFailure: "+t);
                }
            });
        }

        myViewHolder.rowView.setBackgroundColor(Color.WHITE);
    }
}
