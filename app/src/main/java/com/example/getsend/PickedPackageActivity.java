package com.example.getsend;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;


public class PickedPackageActivity extends AppCompatActivity implements View.OnClickListener {

    private String location, packageId, userKey, userName, userPhone, packKey, packageOwnerPhone, packageOwnerId, packageOwnerRate, profileView, packageOwnerName;
    private static final String PACKAGE_STATUS_IN_PROCCESS = "Waiting for approval", DELIMITER = "@";
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;

    private User currUser;
    private TextView edtxt_Size, edtxt_Weight, edtxt_Location, edtxt_Destination, edtxt_PackageId;
    private EditText edtxt_deliverymanNote;
    private Button btn_confirmDelivery, btn_view_profile;
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
        btn_view_profile = findViewById(R.id.btn_viewPofile);
        edtxt_deliverymanNote = findViewById(R.id.edtxt_deliveryNoteID);

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
        btn_view_profile.setOnClickListener(this);
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
                                        packageOwnerRate = String.valueOf(user.getRate());
                                        packageOwnerName = user.getName();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(PickedPackageActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
                                }

                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PickedPackageActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void sendSms(String message) {
        if(checkPermission(Manifest.permission.SEND_SMS)){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(packageOwnerPhone, null, message,null , null);
            Toast.makeText(PickedPackageActivity.this, R.string.sms_send, Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(PickedPackageActivity.this, R.string.sms_did_not_send, Toast.LENGTH_LONG).show();
        }

    }

    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    private void updateCurrUserInSP() {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(currUser);
        prefEditor.putString("currUser", json);
        prefEditor.commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_confirmDeliveryID:
            {
                // integrity input check
                if(edtxt_deliverymanNote.getText().length() > 100){
                    this.edtxt_deliverymanNote.setError(getString(R.string.note_length));
                    this.edtxt_deliverymanNote.requestFocus();
                    return;
                }

                //update package status and deliveryman
                refPackage.child(packKey).child("deliveryman").setValue(userKey);
                refPackage.child(packKey).child("status").setValue(PACKAGE_STATUS_IN_PROCCESS);

                // send sms too package owner that there's a deliveryman
                sendSms("Hi,\n" + currUser.getName() +" deliveryman wants to take your package number: "+ packageId);
                if(!edtxt_deliverymanNote.getText().toString().trim().matches("")){
                    sendSms("Deliveryman Note: " + edtxt_deliverymanNote.getText().toString());
                }
                // add package to the deliveryMan packages
                currUser.setPackagesToDeliver(packKey, userKey);
                updateCurrUserInSP();
                startActivity(new Intent(PickedPackageActivity.this, MainActivity .class));
                finish();
                break;
            }
            case R.id.btn_viewPofile:
            {
                profileView = packageOwnerName +DELIMITER+ packageOwnerRate + DELIMITER +packageOwnerId;
                Intent intent = new Intent(PickedPackageActivity.this, UserProfileViewActivity.class);
                intent.putExtra("profileView", profileView);
                startActivity(intent);
                break;
            }

        }

        }

    //handle device back button
    @Override
    public void onBackPressed() {
        startActivity(new Intent(PickedPackageActivity.this, PickedPackagesListActivity.class));
        finish();
    }
}

