package com.example.getsend;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

public class UserProfileViewActivity extends AppCompatActivity {

    private TextView edtxt_name, edtxt_rate;
    Button btn_close;
    private String[] profileViewA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_view);

        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            String profileView = mBundle.getString("profileView");
            profileViewA = profileView.split("@");
            getIntent().removeExtra("showMessage");
        }

        edtxt_name = findViewById(R.id.edtxt_nameID);
        edtxt_name.setText(profileViewA[0]);

        edtxt_rate = findViewById(R.id.edtxt_rateID);
        switch(profileViewA[1]){
            case "1":
                edtxt_rate.setText("★☆☆☆☆");
                break;
            case "2":
                edtxt_rate.setText("★★☆☆☆");
                break;
            case "3":
                edtxt_rate.setText("★★★☆☆");
                break;
            case "4":
                edtxt_rate.setText("★★★★☆");
                break;
            case "5":
                edtxt_rate.setText("★★★★★");
                break;
            default:
                edtxt_rate.setText("☆☆☆☆☆");
        }

        btn_close = findViewById(R.id.btn_closeID);
        btn_close.setOnClickListener(view -> finish());
    }
}
