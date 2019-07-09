package com.example.tripshare.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tripshare.LoginRegister.User;
import com.example.tripshare.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.tripshare.TripTalk.ChooseFriends.chooseemail;
import static com.example.tripshare.TripTalk.ChooseFriends.choosereason;
import static com.example.tripshare.TripTalk.ChooseFriends.choosetotal;

public class ChooseAdapter extends RecyclerView.Adapter<ChooseAdapter.ChooseViewholder> implements Filterable {
    private static final String TAG = "ChooseAdapter";
    private ArrayList<User> friendlist, friendfulllist;
    private Context mtx;

    public ChooseAdapter(ArrayList<User> friendlist, Context mtx) {
        this.friendlist = friendlist;
        this.mtx = mtx;
        friendfulllist = new ArrayList<>(friendlist);
    }

    @NonNull
    @Override
    public ChooseViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(mtx);
        View view = layoutInflater.inflate(R.layout.choosefriendholder, viewGroup, false);

        return new ChooseViewholder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ChooseViewholder chooseViewholder, int i) {
        Log.d(TAG, "onBindViewHolder: " + choosereason);
        if (choosereason.equals("개설")) {
            //개설인 경우 바로 넣어 줌
        } else {
            //추가인 경우 현재 총원이 1명 이상인 경우와,
            //이미 추가된 친구가 있는 경우를 각각각각 나누어 넣어준다.
            Log.d(TAG, "onBindViewHolder: " + choosetotal);
            if (choosetotal.equals("1")) {
                //총원이 1명이라면 이메일을 배열로 나누지 않고 바로 친구들의 이메일과 비교한다.
                if (chooseemail.equals(friendlist.get(i).getEmail())) {
                    chooseViewholder.linearLayout.setOnTouchListener((v, event) -> true);
                    chooseViewholder.linearLayout.setBackgroundResource(R.color.gray);
                    chooseViewholder.friendcheckBox.setVisibility(View.INVISIBLE);
                }else {
                    chooseViewholder.linearLayout.setOnTouchListener((v, event) -> false);
                    chooseViewholder.linearLayout.setBackgroundResource(R.color.white);
                    chooseViewholder.friendcheckBox.setVisibility(View.VISIBLE);

                }

            } else {
                //총원이 1명 이상인 경우 이메일을 ","을 기준으로 나눈다.
                String[] emailarray = chooseemail.split(",");
                Log.d(TAG, "onBindViewHolder:email " +chooseemail + emailarray.length);

                //for문을 배열 크기만큼 돌려 배열 안의 이메일들을 친구들의 이메일과 비교한다.
                for (String anEmailarray : emailarray) {
                    Log.d(TAG, "onBindViewHolder:anemail "+anEmailarray);
                    Log.d(TAG, "onBindViewHolder:emailfriend "+friendlist.get(i).getEmail());
                    if (anEmailarray.equals(friendlist.get(i).getEmail())) {
                        chooseViewholder.linearLayout.setOnTouchListener((v, event) -> true);
                        chooseViewholder.linearLayout.setBackgroundResource(R.color.gray);
                        chooseViewholder.friendcheckBox.setVisibility(View.INVISIBLE);
                        break;
                    }else {
                        chooseViewholder.linearLayout.setOnTouchListener((v, event) -> false);
                        chooseViewholder.linearLayout.setBackgroundResource(R.color.white);
                        chooseViewholder.friendcheckBox.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        if (friendlist.get(i).isCheckedornot()){
            chooseViewholder.friendcheckBox.setChecked(true);
        }else {
            chooseViewholder.friendcheckBox.setChecked(false);
        }
        chooseViewholder.nametx.setText(friendlist.get(i).getName());
        Glide.with(mtx).load(friendlist.get(i).getImage()).into(chooseViewholder.imgcir);



    }

    @Override
    public int getItemCount() {
        return friendlist.size();
    }

    @Override
    public Filter getFilter() {
        return friendfilter;
    }

    private Filter friendfilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filterdlist = new ArrayList<>();

            //사용자가 입력한 문자가 아무것도 없다면 다 보여줌
            if (constraint == null || constraint.length() == 0) {
                filterdlist.addAll(friendfulllist);
            } else {
                //문자가 있다면 문자열로 바꾼다.
                String filterpattern = constraint.toString().toLowerCase().trim();

                for (User user : friendfulllist) {
                    //사용자가 입력한 이름이 등록된 사용자의 이름에 있는 경우에 리스트에 추가하는 것
                    if (user.getName().toLowerCase().contains(filterpattern)) {
                        filterdlist.add(user);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filterdlist;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            friendlist.clear();
            friendlist.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class ChooseViewholder extends RecyclerView.ViewHolder {
        TextView nametx;
        CircleImageView imgcir;
        LinearLayout linearLayout;
        CheckBox friendcheckBox;

        public ChooseViewholder(@NonNull View itemView) {
            super(itemView);
            nametx = itemView.findViewById(R.id.name_tx_chooseholder);
            imgcir = itemView.findViewById(R.id.img_cir_chooseholder);
            linearLayout = itemView.findViewById(R.id.linear_chooseholder);
            friendcheckBox = itemView.findViewById(R.id.checkbox_chooseholder);

        }
    }
}
