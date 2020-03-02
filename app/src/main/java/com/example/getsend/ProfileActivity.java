package com.example.getsend;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private String userName, phone;
    private TextView edtProfile, edtPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        sharedPref = getSharedPreferences("userName", MODE_PRIVATE);

        //getting the current username from the sp
        userName = sharedPref.getString("name", "");
        phone = sharedPref.getString("phone", "");

        edtProfile = findViewById(R.id.profileNameID);
        edtProfile.setText(userName);

        edtPhone = findViewById(R.id.profilePhoneID);
        edtPhone.setText(phone);
    }
}
