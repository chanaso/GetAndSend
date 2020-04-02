package com.example.getsend;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private String userName, phone, rate;
    private TextView edtProfile, edtPhone, edtRate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);

        //getting the current username from the sp
        userName = sharedPref.getString("name", "");
        phone = sharedPref.getString("phone", "");
        rate = sharedPref.getString("rate", "");

        edtProfile = findViewById(R.id.profileNameID);
        edtProfile.setText(userName);

        edtPhone = findViewById(R.id.profilePhoneID);
        edtPhone.setText(phone);

        edtRate = findViewById(R.id.profileRateID);
        switch(rate){
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
