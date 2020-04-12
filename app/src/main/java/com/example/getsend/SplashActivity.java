package com.example.getsend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity
{
    private static final int DELAY_TIME = 3; // seconds
    private int timeLeft;
    private ProgressBar progressBar;
    private SharedPreferences sharedPref;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBarID);
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        userName = sharedPref.getString("name", "");
        startTimer();
    }

    private void startTimer()
    {
        timeLeft = DELAY_TIME;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while(timeLeft >= 0)
                {
                    progressBar.setProgress(progressBar.getMax()-timeLeft);
                    SystemClock.sleep(1000); //Thread.sleep(1000);
                    timeLeft--;
                }

                if(userName.isEmpty()) {
                    //Open the login activity if there is no user signed in.
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //Open Home activity the user is already registered
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }
        }).start();
    }

}



