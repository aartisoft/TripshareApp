package com.example.tripshare.Adapter;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tripshare.LoginRegister.User;
import com.example.tripshare.ProfileEdit;
import com.example.tripshare.R;
import com.example.tripshare.TripTalk.ChatActivity;
import com.example.tripshare.TripTalk.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewholder> implements Filterable {
    private ArrayList<User> friendlist;
    private ArrayList<User> friendfulllist;
    private Context mtx;

    public FriendsAdapter(ArrayList<User> friendlist, Context mtx) {
        this.friendlist = friendlist;
        this.mtx = mtx;
        friendfulllist = new ArrayList<>(friendlist);
    }

    @NonNull
    @Override
    public FriendsViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(mtx);
        View view = layoutInflater.inflate(R.layout.friendsviewholder, viewGroup, false);

        return new FriendsViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.FriendsViewholder friendsViewholder, int i) {
        friendsViewholder.nametx.setText(friendlist.get(i).getName());
        Glide.with(mtx).load(friendlist.get(i).getImage()).into(friendsViewholder.imgcir);

        friendsViewholder.linearLayout.setOnClickListener(v -> {
            Intent intent = new Intent(mtx, ProfileActivity.class);
            intent.putExtra("name",friendlist.get(i).getName());
            intent.putExtra("email",friendlist.get(i).getEmail());
            intent.putExtra("imgurl",friendlist.get(i).getImage());
            mtx.startActivity(intent);

        });
    }


    @Override
    public int getItemCount() {
        return friendlist.size();
    }

    @Override
    public Filter getFilter() {
        return Filtered;
    }

    private Filter Filtered = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filterdlist = new ArrayList<>();

            //사용자가 입력한 문자가 아무것도 없다면 다 보여줌
            if (constraint ==null || constraint.length() ==0){
                filterdlist.addAll(friendfulllist);
            }else {
                //문자가 있다면 문자열로 바꾼다.
                String filterpattern = constraint.toString().toLowerCase().trim();

                for (User user : friendfulllist){
                    //사용자가 입력한 이름이 등록된 사용자의 이름에 있는 경우에 리스트에 추가하는 것
                    if (user.getName().toLowerCase().contains(filterpattern)){
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

    public class FriendsViewholder extends RecyclerView.ViewHolder {
        TextView nametx;
        CircleImageView imgcir;
        LinearLayout linearLayout;


        public FriendsViewholder(@NonNull View itemView) {
            super(itemView);
            nametx = itemView.findViewById(R.id.name_tx_frholder);
            imgcir = itemView.findViewById(R.id.img_cir_frholder);
            linearLayout = itemView.findViewById(R.id.linear_frholder);
        }
    }
}
