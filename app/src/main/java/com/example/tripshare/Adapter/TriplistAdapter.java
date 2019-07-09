package com.example.tripshare.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Picture;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.tripshare.ApiClient;
import com.example.tripshare.ApiInterface;
import com.example.tripshare.Data.TripData;
import com.example.tripshare.MainActivity;
import com.example.tripshare.R;
import com.example.tripshare.Trip.TourtoolActivity;
import com.example.tripshare.WhereWhen.HowlongActivity;
import com.example.tripshare.WhereWhen.LocationAutocompleteActivity;
import com.example.tripshare.WhereWhen.Trip;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TriplistAdapter extends RecyclerView.Adapter<TriplistAdapter.CustomViewHolder>  {

    private ArrayList<TripData> tripDatalist;
    private Context mContext;
    private String strKorean;
    private String strName;
    private String strEnglish;
    private Integer tnum;
    public static ApiInterface apiInterface;
    CircleImageView circleImageView;
    private static final String TAG = "대사Adapter";
    private Uri photouri;
    private String photostr;
    Picture picture = new Picture();
    SharedPreferences sharedPreferences;
    LinearLayout Triplistlinearlayout;

    SharedPreferences.Editor editor;


    // 1. 컨텍스트 메뉴를 사용하라면 RecyclerView.ViewHolder를 상속받은 클래스에서
// OnCreateContextMenuListener 리스너를 구현해야 합니다.
    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private TextView termtext;
        private TextView triplocationtext;
        LinearLayout Triplistlinearlayout;
        private CircleImageView circle;
        private int tnum;


        public CustomViewHolder(View view) {
            super(view);
            Log.d(TAG, "CostomViewHolder 실행");
            this.termtext = (TextView) view.findViewById(R.id.triplist_term_Textview);
            this.triplocationtext = (TextView) view.findViewById(R.id.triplist_Tripcity_Textview);
            this.Triplistlinearlayout = (LinearLayout) view.findViewById(R.id.triplist_linearlayout);

            //텍스트 뷰에 있는것을 가져옴
            Log.d(TAG, "setOnCreateContextMenuListener(this); 실행");
            view.setOnCreateContextMenuListener(this);
            //2. OnCreateContextMenuListener 리스너를 현재 클래스에서 구현한다고 설정해둡니다.
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            // 3. 컨텍스트 메뉴를 생성하고 메뉴 항목 선택시 호출되는 리스너를 등록해줍니다. ID 1001, 1002로
            // 어떤 메뉴를 선택했는지 리스너에서 구분하게 됩니다.
            Log.d(TAG, "onCreateContextMenu 실행");
            MenuItem locationedit = menu.add(Menu.NONE, 11, 1, "장소 수정");
            MenuItem termedit = menu.add(Menu.NONE, 12, 1, "기간 수정");
            MenuItem delete = menu.add(Menu.NONE, 13, 2, "삭제");

            locationedit.setOnMenuItemClickListener(onEditMenu);
            termedit.setOnMenuItemClickListener(onEditMenu);
            delete.setOnMenuItemClickListener(onEditMenu);
        }

        // 4. 컨텍스트 메뉴 클릭시 동작을 설정
        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                tnum =tripDatalist.get(getAdapterPosition()).getTnum();
                Log.d(TAG, "onMenuItemClick 실행");
                switch (item.getItemId()) {
                    //장소 수정을 선택했을 때
                    case 11:
                        //식별 번호와 arraylist의 몇 번째 데이터를 수정할 건지
                        Log.d(TAG, "onMenuItemClick: tnum : "+tnum);
                        Log.d(TAG, "onMenuItemClick: getadtaterpotiposition() "+getAdapterPosition());
                        Intent intent = new Intent(mContext, LocationAutocompleteActivity.class);
                        intent.putExtra("term",tripDatalist.get(getAdapterPosition()).getTerm());
                        intent.putExtra("way","수정");
                        intent.putExtra("tnum", tnum);
                        intent.putExtra("position", getAdapterPosition());
                        mContext.startActivity(intent);
                        break;
                    case 12:
                        //날짜 수정

                        Log.d(TAG, "onMenuItemClick: tnum : "+tnum);
                        Intent dateintent = new Intent(mContext, HowlongActivity.class);
                        dateintent.putExtra("way", "수정");
                        dateintent.putExtra("tnum", tnum);
                        dateintent.putExtra("placename",tripDatalist.get(getAdapterPosition()).getPlacename());
                        dateintent.putExtra("position", getAdapterPosition());
                        mContext.startActivity(dateintent);
                        break;


                    case 13: // 5. 삭제 항목을 선택시
                        Log.d(TAG, "onMenuItemClick: delete");
                        tripDatalist.remove(getAdapterPosition());
                        // 6. ArratList에서 해당 데이터를 삭제하고

                        Log.d(TAG, "onMenuItemClick: tnum : "+tnum);

                        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

                        Call<Trip> call = apiInterface.Deletemytrip(tnum);
                        call.enqueue(new Callback<Trip>() {
                                         @Override
                                         public void onResponse(Call<Trip> call, Response<Trip> response) {
                                             if (response.isSuccessful()){
                                                 notifyItemRemoved(getAdapterPosition());
                                                 //해당 position item이 제거 됨을 알림
                                                 notifyItemRangeChanged(getAdapterPosition(), tripDatalist.size());
                                                 Log.d(TAG, "onResponse: notifyItemRangeChanged(getAdapterPosition(), tripDatalist.size());");
                                                 Log.d(TAG, "onResponse: 제거 성공"+response.body().getTnum());
                                                 Log.d(TAG, "onResponse: 제거 성공"+response.body().getResponse());

                                             }else{
                                                 Log.d(TAG, "onMenuItemClick: delete error");
                                             }
                                         }
                                         @Override
                                         public void onFailure(Call<Trip> call, Throwable t) {
                                             Log.d(TAG, "onFailure: delete "+t);
                                         }
                                     });
                }
                return true;
            }
        };


    }

    //어뎁터 생성자
    public TriplistAdapter(Context context, ArrayList<TripData> list) {
        tripDatalist = list;
        mContext = context;
        Log.d(TAG, "Triplist 어뎁터 생성자 입력값 확인"+context+list);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {


        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.triplist, null);
        Triplistlinearlayout = view.findViewById(R.id.triplist_linearlayout);

        //item_List를 메모리에 올리고 view에 넣음

        //Triplistlinearlayout.CustomViewHolder viewHolder = new Triplistlinearlayout.CustomViewHolder(view);
        //ViewHolder를 만듬
        // Log.d(TAG, "onCreateViewHolder만들어 해당 xml 인플레이터하고 그 객체를 뷰홀더에 넣음"+viewHolder);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {

        //뷰홀더의 글자 크기 지정
        viewholder.triplocationtext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        viewholder.termtext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        //뷰홀더 글자 위치 지정
        viewholder.triplocationtext.setGravity(Gravity.LEFT);
        viewholder.termtext.setGravity(Gravity.LEFT);


        String term = tripDatalist.get(position).getStartdate()+" ~ "+tripDatalist.get(position).getEnddate()
                +" "+"("+tripDatalist.get(position).getTerm()+"일)";
        //뷰홀더 글자 지정
        viewholder.triplocationtext.setText(tripDatalist.get(position).getPlacename());
        viewholder.termtext.setText(term);

        Log.d(TAG, "onBindViewHolder "+ tripDatalist.get(position).getTerm());
        Log.d(TAG, "onBindViewHolder "+ tripDatalist.get(position).getEnddate());
        Log.d(TAG, "onBindViewHolder "+ tripDatalist.get(position).getPlacename());

        //이미지를 제외한 아이템을 클릭했을 경우 화면이동과 제목,영어대사,해석을 인텐트로 이동
        viewholder.Triplistlinearlayout.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder: countrycode?"+tripDatalist.get(position).getCountrycode());
            Intent intent = new Intent(mContext, TourtoolActivity.class);
            intent.putExtra("term", tripDatalist.get(position).getTerm());
            intent.putExtra("placename", tripDatalist.get(position).getPlacename());
            intent.putExtra("placeid", tripDatalist.get(position).getPlaceid());
            intent.putExtra("tstart", tripDatalist.get(position).getStartdate());
            intent.putExtra("tend", tripDatalist.get(position).getEnddate());
            intent.putExtra("tnum", tripDatalist.get(position).getTnum());
            Log.d(TAG, "onBindViewHolder: latilong"+tripDatalist.get(position).getLatitude()+"\n"+tripDatalist.get(position).getLongitude());
            intent.putExtra("latitude", tripDatalist.get(position).getLatitude());
            intent.putExtra("longitude", tripDatalist.get(position).getLongitude());
            intent.putExtra("countrycode", tripDatalist.get(position).getCountrycode());
            mContext.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        Log.d(TAG, " getItemCount()아이템 값"+ tripDatalist.size());

        return (null != tripDatalist ? tripDatalist.size() : 0);
    }

}