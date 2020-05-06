package com.example.getsend;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class RateUserViewActivity extends Activity {
    private TextView edtxt_user;
    private String[] userDetails;
    private String userName, userKey;
    private int numOfRates;
    private double userRate;
    private RatingBar bar;
    private User currUser;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_user_view);

        // store from local memory the current user
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("currUser", "");
        currUser = gson.fromJson(json, User.class);

        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            String userDetailsString = mBundle.getString("userToRate");
            userDetails = userDetailsString.split("@");
            userName = userDetails[0];
            userKey = userDetails[1];
            userRate = Double.parseDouble(userDetails[2]);
            numOfRates = Integer.parseInt(userDetails[3]) + 1;
            getIntent().removeExtra("showMessage");
        }

        edtxt_user = findViewById(R.id.edtxt_userToRateID);
        edtxt_user.setText("Please rate " + userName + " :)");
        bar = findViewById(R.id.ratingbarID);
        findViewById(R.id.btn_sendID).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(bar.getRating() != 0.0){
                    // set the new rate
                    double newRate = (bar.getRating() + userRate)/numOfRates;
                    currUser.getRefUser().child(userKey).child("rate").setValue(newRate);

                    // add (1) number of rates to user
                    currUser.getRefUser().child(userKey).child("numOfRates").setValue(numOfRates);

                    currUser.setRate(newRate);
                    // update rate in the local memory
                    updateCurrUserInSP();
                }
                finish();
            }
        });

    }
    private void updateCurrUserInSP() {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(currUser);
        prefEditor.putString("currUser", json);
        prefEditor.commit();
    }
}
