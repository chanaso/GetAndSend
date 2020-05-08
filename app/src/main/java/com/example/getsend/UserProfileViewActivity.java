package com.example.getsend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;

public class UserProfileViewActivity extends AppCompatActivity {

    private TextView edtxt_name, edtxt_rate;
    private ImageButton btn_close;
    private String[] profileViewDetails;
    private final String DELIMITER = "@";
    private RatingBar ratingBar;
    private ImageView imageProfile;
    private StorageReference imagesRef;
    private String userKey;
    private final long ONE_MEGABYTE = 1024 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_view);

        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            String profileView = mBundle.getString("profileView");
            // [0] - user name. [1] - user rate. [2] user key
            profileViewDetails = profileView.split(DELIMITER);
            getIntent().removeExtra("showMessage");
        }
        imageProfile = findViewById(R.id.image_ViewProfileID);
        String rate = profileViewDetails[1];
        userKey = profileViewDetails[2];
        showImg();

        edtxt_name = findViewById(R.id.edtxt_nameID);
        edtxt_name.setText(profileViewDetails[0]);

        DecimalFormat df = new DecimalFormat("#.##");

        edtxt_rate = findViewById(R.id.edtxt_rateID);
        edtxt_rate.setText(df.format((Float .parseFloat(rate))));

        ratingBar = findViewById(R.id.ratingBar_viewID);
        ratingBar.setRating(Float.parseFloat(rate));

        btn_close = findViewById(R.id.btn_closeID);
        btn_close.setOnClickListener(view -> finish());
    }

    private void showImg() {
        imagesRef = FirebaseStorage.getInstance().getReference("Images/"+ userKey + ".jpg");
        imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            imageProfile.setImageBitmap(bm);
        }).addOnFailureListener(exception -> Toast.makeText(UserProfileViewActivity.this,R.string.error_message , Toast.LENGTH_SHORT));
    }
}
