package com.example.tripshare.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.example.tripshare.Trip.TourtoolActivity.mydborgplace;

import com.bumptech.glide.Glide;
import com.example.tripshare.Data.Results;
import com.example.tripshare.R;

import java.util.ArrayList;

public class HotPlaceAdapter extends RecyclerView.Adapter<HotPlaceAdapter.HotViewHolder> {

    private ArrayList<Results> hotplresultslist;
    private Context mcontext;
    private static final String TAG = "HotPlaceAdapter";

    public HotPlaceAdapter(Context context, ArrayList<Results> results) {
        this.mcontext = context;
        this.hotplresultslist = results;

    }

    @NonNull
    @Override
    public HotViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        if (mydborgplace.equals("mydb")) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View view = layoutInflater.inflate(R.layout.cardview_spot, viewGroup, false);
            return new HotViewHolder(view);
        } else if (mydborgplace.equals("gplace")) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View view = layoutInflater.inflate(R.layout.cardview_search, viewGroup, false);
            return new HotViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull HotPlaceAdapter.HotViewHolder hotViewHolder, int i) {
        //내 db에 저장된 데이터를 view홀더에 넣어줌
        if (mydborgplace.equals("mydb")){

            //이미지가 없다면 기본이미지
            if (!hotplresultslist.get(i).getUrl().equals("none")){
                Glide.with(mcontext).load(hotplresultslist.get(i).getUrl()).into(hotViewHolder.imgplace);
            }

            hotViewHolder.category.setText(hotplresultslist.get(i).getCategory());
            hotViewHolder.nametx.setText(hotplresultslist.get(i).getName());

            String rating = "평점/ 일정에 등록된 총 수 : "+String.valueOf(hotplresultslist.get(i).getRating())+" /";
            String total = String.valueOf(hotplresultslist.get(i).getUser_ratings_total());
            hotViewHolder.rating.setText(rating);
            hotViewHolder.totaltx.setText(total);
        //place에 데이터 요청
        }else if (mydborgplace.equals("gplace")){
            String rating = "평정 : "+String.valueOf(hotplresultslist.get(i).getRating());
            String total = "총 평가 개수 : "+String.valueOf(hotplresultslist.get(i).getUser_ratings_total());
            hotViewHolder.gplrating.setText(rating);
            hotViewHolder.gplnametx.setText(hotplresultslist.get(i).getName());
            hotViewHolder.gpltotal.setText(total);

        }

    }


    @Override
    public int getItemCount() {
        return hotplresultslist.size();
    }

    public class HotViewHolder extends RecyclerView.ViewHolder {

        //내 디비에서 가져온 장소에 대한 정보들을 넣을 공간
        TextView nametx, totaltx, rating, category;
        ImageView imgplace;
        //구글에서 가져온 장소에 대한 정보들을 넣을 뷰
        TextView gplnametx, gpltotal, gplrating;

        public HotViewHolder(@NonNull View itemView) {
            super(itemView);
            if (mydborgplace.equals("mydb")) {
                //내 디비에서 가져오는 경우
                nametx = itemView.findViewById(R.id.name_cardview_spot);
                totaltx = itemView.findViewById(R.id.total_cardview_spot);
                rating = itemView.findViewById(R.id.rate_cardview_spot);
                category = itemView.findViewById(R.id.category_cardview_spot);
                imgplace = itemView.findViewById(R.id.place_img_spot);
            } else if (mydborgplace.equals("gplace")) {
                //구글에서 가져오는 경우
                gplnametx = itemView.findViewById(R.id.name_cardview_search);
                gpltotal = itemView.findViewById(R.id.total_cardview_search);
                gplrating = itemView.findViewById(R.id.rating_cardview_search);
            }
        }
    }
}
