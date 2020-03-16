package com.example.getsend;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InviteDeliveryActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText edtxtWeight;
    private EditText edtxtSize;
    private EditText edtxtLocation;
    private EditText edtxtDestination;
    private Button btnEnter;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_delivery);

        edtxtWeight = findViewById(R.id.edtxt_weight);
        edtxtSize = findViewById(R.id.edtxt_size);
        edtxtLocation = findViewById(R.id.edtxt_location);
        edtxtDestination = findViewById(R.id.edtxt_destination);
        btnEnter = (Button) findViewById(R.id.btn_enter);

        ref = FirebaseDatabase.getInstance().getReference().child("Package");
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        final String size = edtxtSize.getText().toString().trim();
        final String location = edtxtLocation.getText().toString().trim();
        final String destination = edtxtDestination.getText().toString().trim();
        final double weight = Double.parseDouble(edtxtWeight.getText().toString());
        //edtxtWeight
        Package new_package = new Package(size, location, destination, weight);
    }
}
