package com.example.tripshare.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tripshare.Data.Weather;
import com.example.tripshare.R;

import java.util.ArrayList;

public class TourtoolAdapter extends RecyclerView.Adapter<TourtoolAdapter.WeatherViewholder> {
   private Context mcontext;
   private ArrayList<Weather> mweatherlist;
    private static final String TAG = "TourtoolAdapter";
    public TourtoolAdapter(Context context, ArrayList<Weather> weatherArrayList) {
    this.mcontext = context;
    this.mweatherlist = weatherArrayList;

    }

    @NonNull
    @Override
    public WeatherViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.cardview_weather,viewGroup,false);
        return new WeatherViewholder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull TourtoolAdapter.WeatherViewholder weatherViewholder, int i) {
        //3시간 동안 측정된 강수량을 더한다.
        double rain = 0;
        for (int raindrop =0 ; raindrop<8; raindrop++){
           rain = rain+Double.valueOf(mweatherlist.get(i).getHourly().get(raindrop).getRainperhour());
        }
        Log.d(TAG, "onBindViewHolder: "+rain);
        //소수점 첫째까지 나타내기 위해 두 번째에서 반올림한다.
        @SuppressLint("DefaultLocale") String banorlinraindrop = String.format("%.1f",rain);
        Log.d(TAG, "onBindViewHolder: "+banorlinraindrop);

        String totalraindrop = "총 강수량 : "+banorlinraindrop+"mm";
        String maxmintemp = "최소/최대 온도 : "+mweatherlist.get(i).getMintempC()+" / "+mweatherlist.get(i).getMaxtempC()+" °C";
        String subtitle = mweatherlist.get(i).getDate().substring(5);

        weatherViewholder.maxtx.setText(maxmintemp);
        weatherViewholder.datetx.setText(subtitle);
        weatherViewholder.raintx.setText(totalraindrop);


        //하루 총 강수량이 1mm를 넘으면 이미지를 비오는 이미지로 바꿔줌
        if (rain > 5){
            Glide.with(mcontext).load("http://cdn.worldweatheronline.net/images/wsymbols01_png_64/wsymbol_0034_cloudy_with_heavy_rain_night.png")
                    .into(weatherViewholder.imgweather);
        }else {
        //2mm도 안 오면 오후 12시 날씨 이미지로 해준다.
            Glide.with(mcontext).load(mweatherlist.get(i).getHourly().get(4).getWeatherIconUrl().get(0).getValue())
                    .into(weatherViewholder.imgweather);
        }


    }

    @Override
    public int getItemCount() {
        return mweatherlist.size();
    }

    public class WeatherViewholder extends RecyclerView.ViewHolder {
    ImageView imgweather;
    TextView maxtx, raintx,datetx;

        public WeatherViewholder(@NonNull View itemView) {
            super(itemView);
            imgweather = itemView.findViewById(R.id.img_tourtool);
            maxtx = itemView.findViewById(R.id.tx_max_tourtool);
            raintx = itemView.findViewById(R.id.rain_tourtool);
            datetx = itemView.findViewById(R.id.oneday_tx_tourtool);
        }
    }
}
