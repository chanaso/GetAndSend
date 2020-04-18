package com.example.getsend;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class PowerOfAttorney extends AppCompatActivity {

    Signature signature;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signature = new Signature(this, null);
        setContentView(signature);
    }
}
