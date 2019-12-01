package com.example.getsend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity
{
    private static final int DELAY_TIME = 3; // seconds
    private int timeLeft;
    private ProgressBar progressBar;
    private SharedPreferences sharedPref;
    private int number;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBarID);
        sharedPref = getSharedPreferences("data",MODE_PRIVATE);
        number = sharedPref.getInt("isLogged", 0);
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
                if(number == 0) {
                    //Open the login activity and set this so that next it value is 1 then this conditin will be false.
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //Open this Home activity
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }
        }).start();
    }

}



