package com.example.getsend;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;

public class PickedPackageActivity extends AppCompatActivity {
    private String pickedPackageLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_picked);
        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            pickedPackageLocation = mBundle.getString("packageLocation");
            getIntent().removeExtra("showMessage");
            Toast.makeText(PickedPackageActivity.this, pickedPackageLocation, Toast.LENGTH_LONG).show();

        }
    }
}
