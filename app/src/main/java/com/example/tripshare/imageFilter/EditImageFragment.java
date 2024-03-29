package com.example.tripshare.imageFilter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.example.tripshare.R;
import com.example.tripshare.imageFilter.Interface.EditImageFragmentListener;


public class EditImageFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {


    private EditImageFragmentListener listener;
    SeekBar seekBar_brightness, seekBar_constrant, seekBar_saturation;

    public void setListener(EditImageFragmentListener listener) {
        this.listener = listener;
    }

    public EditImageFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView=  inflater.inflate(R.layout.fragment_edit_image, container, false);

        seekBar_brightness = (SeekBar) itemView.findViewById(R.id.seekbar_brightness);
        seekBar_constrant = itemView.findViewById(R.id.seekbar_constrast);
        seekBar_saturation = itemView.findViewById(R.id.seekbar_saturation);

        seekBar_brightness.setMax(200);
        seekBar_brightness.setProgress(100);

        seekBar_constrant.setMax(20);
        seekBar_constrant.setProgress(0);

        seekBar_saturation.setMax(30);
        seekBar_saturation.setProgress(10);

        seekBar_saturation.setOnSeekBarChangeListener(this);
        seekBar_constrant.setOnSeekBarChangeListener(this);
        seekBar_brightness.setOnSeekBarChangeListener(this);
        return itemView   ;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (listener != null){
            if (seekBar.getId() == R.id.seekbar_brightness){
                listener.onBrightnessChanged(progress-100);
            }else if (seekBar.getId() == R.id.seekbar_constrast){
                progress +=10;
                float value = .10f*progress;
                listener.onConstrantChanged(value);
            }else if (seekBar.getId() == R.id.seekbar_saturation){
                float value = .10f*progress;
                listener.onSaturationChanged(value);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener != null){
            listener.onEditStarted();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener !=null){
            listener.onEditCompleted();
        }
    }

    public void resetControls(){
        seekBar_brightness.setProgress(100);
        seekBar_constrant.setProgress(0);
        seekBar_saturation.setProgress(10);
    }
}
