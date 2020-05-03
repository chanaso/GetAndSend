package com.example.getsend;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

public class PackageActivity extends AppCompatActivity{
    private final int DELIVERYMAN = 0, OWNER = 1;
    private TextView edtxt_Size, edtxt_Weight, edtxt_Location, edtxt_Destination, edtxt_delivery, edtxt_Status, edtxt_PackageId;
    private Package pack;
    private Button btn_1, btn_2, btn_confirm;
    private User currUser;
    private SharedPreferences sharedPref;
    private String userKey;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_package);

            // store from local memory the current user
            sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPref.getString("currUser", "");
            currUser = gson.fromJson(json, User.class);
            userKey = sharedPref.getString("userKey", "");

            btn_1 = (Button) findViewById(R.id.btn_1);
            btn_2 = (Button) findViewById(R.id.btn_2);
            btn_confirm = (Button) findViewById(R.id.btn_confirm);

            Bundle mBundle = getIntent().getExtras();
            if (mBundle != null) {
                String packStr = mBundle.getString("package");
                getIntent().removeExtra("showMessage");

                // convert json to Package object
                Gson gson_2 = new Gson();
                pack = gson_2.fromJson(packStr , Package.class);
            }

            edtxt_PackageId = findViewById(R.id.edtxt_packageID);
            edtxt_PackageId.setText(pack.getPackageId()+"");

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

            if(currUser.getType() == OWNER) {
                switch (pack.getStatus()) {
                    case "Waiting for delivery":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setVisibility(View.INVISIBLE);
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Delete package option

                            }
                        });
                        break;
                    case "Waiting for approval":
                        btn_1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //View deliveryman details
                            }
                        });
                        btn_2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Reject delivery
                            }
                        });
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Approve delivery
                            }
                        });
                        break;
                    case "On the way...":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Open chat
                            }
                        });
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Delivery confirmation
                            }
                        });
                        break;
                    case "Arrived :)":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setVisibility(View.INVISIBLE);
                        btn_confirm.setVisibility(View.INVISIBLE);
                        break;
                }
            }
            else{ //DELIVERYMAN type
                switch (pack.getStatus()) {
                    case "Waiting for delivery":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //View owner details
                            }
                        });
                        btn_confirm.setVisibility(View.INVISIBLE);
                        break;
                    case "Waiting for approval":
                    case "Arrived :)":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setVisibility(View.INVISIBLE);
                        btn_confirm.setVisibility(View.INVISIBLE);
                        break;
                    case "On the way...":
                        btn_1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //view POA
                            }
                        });
                        btn_2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Open chat
                            }
                        });
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Delivery confirmation
                            }
                        });
                        break;
                }
             }
    }
}
