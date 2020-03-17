package com.example.getsend;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InviteDeliveryActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText edtxtWeight;
    private EditText edtxtSize;
    private EditText edtxtLocation;
    private EditText edtxtDestination;
    private Button btnEnter;
    private DatabaseReference reff;
    private Package new_package;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_delivery);

        edtxtWeight = (EditText) findViewById(R.id.edtxt_weight);
        edtxtSize = (EditText) findViewById(R.id.edtxt_size);
        edtxtLocation =(EditText) findViewById(R.id.edtxt_location);
        edtxtDestination = (EditText) findViewById(R.id.edtxt_destination);
        btnEnter = (Button) findViewById(R.id.btn_enter);
        //create a new DB table of package if not exist
        reff = FirebaseDatabase.getInstance().getReference().child("Package");

        btnEnter.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        //create a new object of package
        final String size = edtxtSize.getText().toString().trim();
        final String location = edtxtLocation.getText().toString().trim();
        final String destination = edtxtDestination.getText().toString().trim();
        final double weight = Double.parseDouble(edtxtWeight.getText().toString());

        new_package = new Package(size, location, destination, weight);

        //push package to DB
        reff.push().setValue(new_package);
        Toast.makeText(InviteDeliveryActivity.this, "Package registered successfully!", Toast.LENGTH_LONG).show();
    }
}
