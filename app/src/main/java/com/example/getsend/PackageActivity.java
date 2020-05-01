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
    private TextView edtxt_Size, edtxt_Weight, edtxt_Location, edtxt_Destination, edtxt_delivery, edtxt_Status, edtxt_PackageId;
    private Package pack;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_package);

        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            String packStr = mBundle.getString("package");
            getIntent().removeExtra("showMessage");

            // convert json to Package object
            Gson gson = new Gson();
            pack = gson.fromJson(packStr , Package.class);
        }

        edtxt_PackageId = findViewById(R.id.edtxt_packageID);
        edtxt_PackageId.setText(pack.getPackageId());

        edtxt_Size = findViewById(R.id.edtxt_SizeID);
        edtxt_Size.setText(pack.getSize());

        edtxt_Weight = findViewById(R.id.edtxt_WeightID);
        edtxt_Weight.setText(pack.getWeight()+"");

        edtxt_Location = findViewById(R.id.edtxt_LocationID);
        edtxt_Location.setText(pack.getLocation());

        edtxt_Destination = findViewById(R.id.edtxt_DestinationID);
        edtxt_Destination.setText(pack.getDestination());

        if(!pack.getDeliveryman().isEmpty()) {
            edtxt_delivery = findViewById(R.id.edtxt_DeliveryID);
            edtxt_delivery.setVisibility(View.VISIBLE);
            edtxt_delivery.setText(pack.getDeliveryman());
        }

        edtxt_Status = findViewById(R.id.edtxt_StatusID);
        edtxt_Status.setText(pack.getStatus());

    }
}
