package com.example.tripshare.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.example.tripshare.Data.Results;
import com.example.tripshare.R;


import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ResultsViewholder> implements NumberPicker.OnValueChangeListener {

    private ArrayList<Results> resultsArrayList;
    private static final String TAG = "SearchAdapter";
    private Context mcontext;
    public SearchAdapter(Context context,ArrayList<Results> results) {
        Log.d(TAG, "SearchAdapter: ");
        this.resultsArrayList = results;
        this.mcontext = context;
    }

    @Override
    public ResultsViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.cardview_search, parent, false);
        return new ResultsViewholder(view);
    }

    @Override
    public void onBindViewHolder(ResultsViewholder holder, int position) {
     //     String term = dataList.get(position).getStartdate()+" ~ "+dataList.get(position).getEnddate()
     //           +" "+"("+dataList.get(position).getTerm()+"일)";
        Log.d(TAG, "onBindViewHolder: "+position);
        Log.d(TAG, "onBindViewHolder:size "+resultsArrayList.size());
        Log.d(TAG, "onBindViewHolder:name "+resultsArrayList.get(position).getName());
        String name ="이름 : "+resultsArrayList.get(position).getName();
        String rating = "평점 : "+resultsArrayList.get(position).getRating();
        String total = "총 평가 개수 : "+resultsArrayList.get(position).getUser_ratings_total();
        holder.restrauntname.setText(name);
        holder.rating.setText(rating);
        holder.totalrating.setText(total);

//        AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(mcontext, R.style.myDialog));
//        View view = LayoutInflater.from(mcontext).inflate(R.layout.itinerarydialog,null,false);
//
//        dialog.setView(view);
//
//        TextView textView = view.findViewById(R.id.delete_Itinerarydialog_tx);
//        Button button = view.findViewById(R.id.yes_itinerarydialog_bt);
//        Button nobutton = view.findViewById(R.id.no_itinerarydialog_bt);
//
//       final AlertDialog alertDialog = dialog.create();
//        //        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
////        dialog.setContentView(R.layout.itinerarydialog);
//        holder.restrauntname.setOnClickListener(v -> {
//
//        alertDialog.show();
// });



//       PhotoMetadata placePhoto = resultsArrayList.get(position).getPlacePhoto();
//        Log.d(TAG, "onBindViewHolder: "+resultsArrayList.get(position).getPlacePhoto());
//        PlacesClient placesClient = Places.createClient(mcontext);
//
//        FetchPhotoRequest.Builder photoRequestBuilder = FetchPhotoRequest.builder(placePhoto);
//
//        Task<FetchPhotoResponse> photoTask = placesClient.fetchPhoto(photoRequestBuilder.build());
//
//        photoTask.addOnSuccessListener(
//                response -> {
//                    Log.d(TAG, "fetchPhoto:response "+response);
//                    Log.d(TAG, "fetchPhoto:bitmap "+response.getBitmaptoString());
//                    holder.imageView.setImageBitmap(response.getBitmaptoString());
//                });
//
//        photoTask.addOnFailureListener(
//                exception -> {
//                    Log.d(TAG, "onBindViewHolder: ");
//                    exception.printStackTrace();
//                });
//
//        photoTask.addOnCompleteListener(response -> Toast.makeText(mcontext, "사진이 없습니다.", Toast.LENGTH_SHORT).show());

    }




    @Override
    public int getItemCount() {
        return resultsArrayList.size();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }

    class ResultsViewholder extends RecyclerView.ViewHolder {

        TextView restrauntname, rating, totalrating;
       // ImageView imageView;
        ResultsViewholder(View itemView) {
            super(itemView);
            restrauntname = (TextView) itemView.findViewById(R.id.name_cardview_search);
            rating = (TextView) itemView.findViewById(R.id.rating_cardview_search);
            totalrating = (TextView) itemView.findViewById(R.id.total_cardview_search);
            //imageView = itemView.findViewById(R.id.img_search);
        }
    }
}
