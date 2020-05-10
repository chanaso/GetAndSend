package com.example.getsend;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.text.DecimalFormat;


public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private User currUser;
    private TextView edtProfile, edtPhone, edtRate;
    private RatingBar ratingBar;
    private StorageReference imagesRef;
    private String userKey;
    final long ONE_MEGABYTE = 1024 * 1024 * 5;
    private ImageView imageView;

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
        userKey = sharedPref.getString("userKey", "");

        imageView = findViewById(R.id.image_ViewProfileID);
        showImg();

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

    //get the user profile picture from the storage
    private void showImg() {
        imagesRef = FirebaseStorage.getInstance().getReference("Images/"+ userKey + ".jpg");
        imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            imageView.setImageBitmap(bm);
        }).addOnFailureListener(exception -> Toast.makeText(ProfileActivity.this, R.string.error_message , Toast.LENGTH_SHORT));
    }

}
