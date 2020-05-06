package com.example.getsend;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserProfileViewActivity extends AppCompatActivity {

    private TextView edtxt_name, edtxt_rate;
    private ImageButton btn_close;
    private String[] profileViewDetails;
    private final String DELIMITER = "@";
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_view);

        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            String profileView = mBundle.getString("profileView");
            // [0] - user name. [1] - user rate.
            profileViewDetails = profileView.split(DELIMITER);
            getIntent().removeExtra("showMessage");
        }

        String rate = profileViewDetails[1];
        edtxt_name = findViewById(R.id.edtxt_nameID);
        edtxt_name.setText(profileViewDetails[0]);

        edtxt_rate = findViewById(R.id.edtxt_rateID);
        edtxt_rate.setText(rate);

        ratingBar = findViewById(R.id.ratingBar_viewID);
        ratingBar.setRating(Float.parseFloat(rate));

        btn_close = findViewById(R.id.btn_closeID);
        btn_close.setOnClickListener(view -> finish());
    }
}
