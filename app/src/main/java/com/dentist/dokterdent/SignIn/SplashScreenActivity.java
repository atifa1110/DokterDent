package com.dentist.dokterdent.SignIn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.dentist.dokterdent.R;

public class SplashScreenActivity extends AppCompatActivity {

    private Handler mDelayHandler;
    private int SPLASH_DELAY = 2000;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        runnable = new Runnable(){
            @Override
            public void run() {
                navigateToIntroActivitiy();
            }
        };

        mDelayHandler = new Handler();
        mDelayHandler.postDelayed(runnable,SPLASH_DELAY);
    }

    private void navigateToIntroActivitiy() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }

    protected void onPause() {
        super.onPause();
        finish();
    }

    protected void onDestroy() {
        if (mDelayHandler != null) {
            mDelayHandler.removeCallbacks(runnable);
        }
        super.onDestroy();
    }
}