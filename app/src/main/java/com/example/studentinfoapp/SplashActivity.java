package com.example.studentinfoapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 sec

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View logo = findViewById(R.id.logoRectangle);
        TextView title = findViewById(R.id.tvAppName);

        // Fade-in animation
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1500);
        logo.startAnimation(fadeIn);
        title.startAnimation(fadeIn);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);

    }
}
