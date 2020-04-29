package com.example.getsend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;

import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;


public class PickedPackageActivity extends AppCompatActivity implements View.OnClickListener {

    private String location, packageId, userKey, userName, userPhone, packKey, packageOwnerPhone, packageOwnerId;
    private static final String PACKAGE_STATUS_IN_PROCCESS = "In proccess!";
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;

    private User currUser;
    private TextView edtxt_Size, edtxt_Weight, edtxt_Location, edtxt_Destination, edtxt_PackageId, edtxt_packageOwner;
    private EditText edtxt_deliverymanNote, edtxt_deliverymanId;
    private Button btn_confirmDelivery;
    private SharedPreferences sharedPref;
    private DatabaseReference refPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picked_package);

        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            String[] pickedPackage = mBundle.getString("pickedPackage").split("@");
            location = pickedPackage[0];
            packageId = pickedPackage[1];
            getIntent().removeExtra("showMessage");
        }
        edtxt_PackageId = findViewById(R.id.edtxt_packageID);
        edtxt_PackageId.setText(packageId);
        edtxt_Location = findViewById(R.id.edtxt_LocationID);
        edtxt_Location.setText(location);

        edtxt_Size = findViewById(R.id.edtxt_SizeID);
        edtxt_Weight = findViewById(R.id.edtxt_WeightID);
        edtxt_Destination = findViewById(R.id.edtxt_DestinationID);
        btn_confirmDelivery = findViewById(R.id.btn_confirmDeliveryID);
        edtxt_packageOwner = findViewById(R.id.edtxt_packageOwnerID);
        edtxt_deliverymanNote = findViewById(R.id.edtxt_deliveryNoteID);
        edtxt_deliverymanId = findViewById(R.id.edtxt_deliverymanIdID);

        // store from local memory the current user
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("currUser", "");
        currUser = gson.fromJson(json, User.class);
        userKey = sharedPref.getString("userKey", "");

        refPackage = FirebaseDatabase.getInstance().getReference().child("Package");
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);

        setTxtViews();
        btn_confirmDelivery.setOnClickListener(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }


    private void setTxtViews() {
        refPackage.orderByChild("packageId").equalTo(packageId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot datas : dataSnapshot.getChildren()) {
                        Package pack = datas.getValue(Package.class);
                        // the package that clicked on the list
                        if (pack.getLocation().equals(location)) {
                            edtxt_Weight.setText(pack.getWeight() + "");
                            edtxt_Size.setText(pack.getSize());
                            edtxt_Destination.setText(pack.getDestination());
                            packKey = datas.getKey();
                            packageOwnerId = pack.getPackageOwnerId();
                            currUser.getRefUser().child(packageOwnerId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        User user = dataSnapshot.getValue(User.class);
                                        packageOwnerPhone = user.getPhone();
                                        edtxt_packageOwner.setText(user.getName());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }

                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendSms() {
        if(checkPermission(Manifest.permission.SEND_SMS)){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(packageOwnerPhone, null,"Hi,\n"+ userName+" deliveryman wants to take your package number: "+ packageId+"\nplease enter GetAndSend app and confirm the delivery!",null , null);
            smsManager.sendTextMessage(packageOwnerPhone, null, "Deliveryman Note: "+edtxt_deliverymanNote.getText().toString(),null , null);
            Toast.makeText(PickedPackageActivity.this, "SMS send successfully", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(PickedPackageActivity.this, "SMS did not sended to the package owner", Toast.LENGTH_LONG).show();
        }

    }

    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onClick(View view) {
        // integrity input check
        if(edtxt_deliverymanId.getText().length() !=9){
            this.edtxt_deliverymanId.setError("An Israeli ID must contain 9 numbers:");
            this.edtxt_deliverymanId.requestFocus();
            return;
        }
        if(edtxt_deliverymanNote.getText().length() > 100){
            this.edtxt_deliverymanNote.setError("Note should be less than 160 letters");
            this.edtxt_deliverymanNote.requestFocus();
            return;
        }

        //update package status and deliveryman
        refPackage.child(packKey).child("deliveryman").setValue(userKey);
        refPackage.child(packKey).child("status").setValue(PACKAGE_STATUS_IN_PROCCESS);

        // send sms too package owner that theres a deliverman
        sendSms();
        // add package to the deliveryMan packages
        currUser.setPackages(packKey, userKey);
        startActivity(new Intent(PickedPackageActivity.this, MainActivity .class));
        finish();
        }


}

