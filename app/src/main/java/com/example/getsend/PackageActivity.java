package com.example.getsend;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class PackageActivity extends AppCompatActivity {
    private TextView edtSize, edtWeight, edtLocation, edtDestination, edtdelivery, edtStatus;
    private Package pack;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_package);

        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            String packStr = mBundle.getString("package");
            // convert json to Package object
            Gson gson = new Gson();
            pack = gson.fromJson(packStr , Package.class);
        }

        edtSize = findViewById(R.id.edtSizeID);
        edtSize.setText(pack.getSize());

        edtWeight = findViewById(R.id.edtWeightID);
        edtWeight.setText(String.valueOf(pack.getWeight()));

        edtLocation = findViewById(R.id.edtLocationID);
        edtLocation.setText(pack.getLocation());

        edtDestination = findViewById(R.id.edtDestinationID);
        edtDestination.setText(pack.getDestination());

        if(!pack.getDeliveryman().equals("")) {
            edtdelivery = findViewById(R.id.edtDeliveryID);
            edtdelivery.setVisibility(View.VISIBLE);
            edtdelivery.setText(pack.getDeliveryman());
        }

        edtStatus = findViewById(R.id.edtStatusID);
        edtStatus.setText(pack.getStatus());


    }
}
