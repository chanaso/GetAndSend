package com.example.getsend;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.text.DecimalFormat;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private User currUser;
    private TextView edtProfile, edtPhone, edtRate;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);

        //getting the current username from the sp
        // store from local memory the current user
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("currUser", "");
        currUser = gson.fromJson(json, User.class);

        edtProfile = findViewById(R.id.profileNameID);
        edtProfile.setText(currUser.getName());

        edtPhone = findViewById(R.id.profilePhoneID);
        edtPhone.setText(currUser.getPhone());

        DecimalFormat df = new DecimalFormat("#.##");

        edtRate = findViewById(R.id.edtRateID);
        edtRate.setText(df.format(currUser.getRate()));

        ratingBar = findViewById(R.id.ratingBar_profileID);
        ratingBar.setRating((float)currUser.getRate());
    }


}
