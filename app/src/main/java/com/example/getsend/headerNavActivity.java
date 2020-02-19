package com.example.getsend;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ResourceBundle;

public class headerNavActivity extends AppCompatActivity {

    private String userName;
    private TextView edtTxt;
    private SharedPreferences sharedpref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_header);
        // get the saved username from shared preferences
        SharedPreferences sh = getSharedPreferences("userName", MODE_PRIVATE);
        userName = sh.getString("name", "");
        // set reference to the text view
        edtTxt = (TextView) findViewById(R.id.UserNameID);
        // set the string from sp as text of the textview
        Log.d("userName", userName);
        edtTxt.setText("hello");
    }
}
