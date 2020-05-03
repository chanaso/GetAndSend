package com.example.getsend;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RateUserViewActivity extends Activity {
    private TextView edtxt_user;
    private String[] userDetails;
    private String userName, userKey;
    private int numOfRates, userRate;
    private RatingBar bar;
    private DatabaseReference refUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_user_view);

        refUser = FirebaseDatabase.getInstance().getReference().child("User");
        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            String userDetailsString = mBundle.getString("userToRate");

            userDetails = userDetailsString.split("@");
            userName = userDetails[0];
            userKey = userDetails[1];
            userRate = Integer.parseInt(userDetails[2]);
            numOfRates = Integer.parseInt(userDetails[3]) + 1;
            getIntent().removeExtra("showMessage");
        }
        edtxt_user = findViewById(R.id.edtxt_userToRateID);
        edtxt_user.setText("Please rate " + userName + " :)");
        bar = findViewById(R.id.ratingbarID);
        findViewById(R.id.btn_sendID).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Toast.makeText(RateUserViewActivity.this,  bar.getRating()+"", Toast.LENGTH_LONG).show();
                if(bar.getRating() != 0.0){
                    // set the new rate
                    refUser.child(userKey).child("rate").setValue((bar.getRating() + userRate)/numOfRates);

                    // add (1) number of rates to user
                    refUser.child(userKey).child("numOfRates").setValue(numOfRates);
                }
            }
        });

    }
}
