package com.example.getsend;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private User currUser;
    private TextView edtProfile, edtPhone, edtRate;
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

        edtRate = findViewById(R.id.profileRateID);
        switch(String.valueOf(currUser.getRate())){
            case "1":
                edtRate.setText("★☆☆☆☆");
                break;
            case "2":
                edtRate.setText("★★☆☆☆");
                break;
            case "3":
                edtRate.setText("★★★☆☆");
                break;
            case "4":
                edtRate.setText("★★★★☆");
                break;
            case "5":
                edtRate.setText("★★★★★");
                break;
            default:
                edtRate.setText("☆☆☆☆☆");
        }
    }
}
