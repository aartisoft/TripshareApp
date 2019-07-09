package com.example.tripshare.imageFilter;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tripshare.R;
import com.example.tripshare.imageFilter.Adapter.ThumbnailAdapter;
import com.example.tripshare.imageFilter.Interface.FiltersListFragmentListener;
import com.example.tripshare.imageFilter.Utils.SpacesItemDecoration;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FiltersListFragment extends Fragment implements FiltersListFragmentListener {
    RecyclerView recyclerView;
    ThumbnailAdapter adapter;
    List<ThumbnailItem> thumbnailItems;

    FiltersListFragmentListener listener;
    Bitmap thumbImg;
    public void setListener(FiltersListFragmentListener listener) {
        this.listener = listener;
    }

    public FiltersListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView =  inflater.inflate(R.layout.fragment_filters_list, container, false);

        thumbnailItems = new ArrayList<>();
        adapter = new ThumbnailAdapter(thumbnailItems, this, getActivity());
        recyclerView = (RecyclerView) itemView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(adapter);

        displayThumbnail(null);
        return itemView;
    }

    public void displayThumbnail(final Bitmap bitmap) {
        Runnable r = () -> {
            if (bitmap == null){
                try {
                    thumbImg = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(FilterActivity.uri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                    thumbImg = BitmapUtils.getBitmapFromAssets(getActivity(), MainActivity.pictureName,100,100);
            }else {
                thumbImg = Bitmap.createScaledBitmap(bitmap, 100,100,false);
            }
            if (thumbImg == null){
                return;
            }
            ThumbnailsManager.clearThumbs();
            thumbnailItems.clear();

            //add normal bitmap first
            ThumbnailItem thumbnailItem = new ThumbnailItem();
            thumbnailItem.image = thumbImg;
            thumbnailItem.filterName = "Normal";
            ThumbnailsManager.addThumb(thumbnailItem);

            List<Filter> filters = FilterPack.getFilterPack(getActivity());

            for (Filter filter:filters){
                ThumbnailItem tI = new ThumbnailItem();
                tI.image = thumbImg;
                tI.filter = filter;
                tI.filterName = filter.getName();
                ThumbnailsManager.addThumb(tI);
            }

            thumbnailItems.addAll(ThumbnailsManager.processThumbs(getActivity()));

            getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
        };
        new Thread(r).start();
    }

    @Override
    public void onFilterSelected(Filter filter) {
        if (listener != null){
            listener.onFilterSelected(filter);
        }
    }
}
