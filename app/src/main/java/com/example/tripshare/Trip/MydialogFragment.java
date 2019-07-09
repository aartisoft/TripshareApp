package com.example.tripshare.Trip;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.example.tripshare.R;

public class MydialogFragment extends DialogFragment {
    private static final String TAG = "MydialogFragment";
    private MydialogFragment myListener;
    Button yes,no;
    private int result;
    // 리스너 인터페이스 선언
    public interface MydialogListener{
        public void myCallback(DialogFragment dialogFragment,int result);
 }
    // 리스너 인터페이스의 빈 객체 선언
    MydialogListener mydialogListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mydialogListener = (MydialogListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+" must implement MydialogListner");
        }
    }

    // 리스너 인터페이스 속의 메서드 작동(연결된 액티비티와 통신)
    public void someAction () {
       mydialogListener.myCallback(
                MydialogFragment.this, result);
    }

    public MydialogFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.itinerarydialog, container);
//        //remove dialog title
//        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        //remove dialog background
//      getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //배경화면을 클릭해도 다이어로그가 안 사라짐
        getDialog().setCanceledOnTouchOutside(false);
        //기기에서 뒤로가기를 눌러도 다이어로그가 안 사라짐
        setCancelable(false);

        yes = view.findViewById(R.id.yes_itinerarydialog_bt);
        no = view.findViewById(R.id.no_itinerarydialog_bt);
        yes.setOnClickListener(v -> {
            Log.d(TAG, "onCreateView:cliked ");
            result = 1;
            someAction();

        });
        no.setOnClickListener(v -> {
            result = 0;
            someAction();
            Log.d(TAG, "onCreateView: cliked");

        });
        return view;
    }
}
