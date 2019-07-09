package com.example.tripshare.LiveStream;

import android.animation.Animator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tripshare.R;

public class PracticeActivity extends AppCompatActivity {

    Button button;
    LottieAnimationView lottieAnimationView;
    private static final String TAG = "PracticeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);
        Log.d(TAG, "onCreate: ");
        button = findViewById(R.id.start_bt);
        lottieAnimationView = findViewById(R.id.heart);
        lottieAnimationView.setAnimation("heartt.json");

        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "onAnimationStart: ");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "onAnimationEnd: ");
                lottieAnimationView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d(TAG, "onAnimationCancel: ");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.d(TAG, "onAnimationRepeat: ");
            }
        });
        button.setOnClickListener(v -> {

            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();

        });
    }

}
