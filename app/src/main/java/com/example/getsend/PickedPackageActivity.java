package com.example.getsend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class PickedPackageActivity extends AppCompatActivity implements View.OnClickListener {
    private String location, packageId, userKey, userName, userPhone, packKey, packageOwnerPhone;
    private static final String PACKAGE_STATUS_IN_PROCCESS = "In proccess!";
    private static final int USER_TYPE_IN_PROCCESS = 2;
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 1;



    private TextView edtxt_Size, edtxt_Weight, edtxt_Location, edtxt_Destination, edtxt_PackageId, edtxt_packageOwner;
    private EditText edtxt_deliverymanNote;
    private Button btn_confirmDelivery;
    private SharedPreferences sharedPref;
    private DatabaseReference refUser, refPackage;

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
        // integrity input check
        if(edtxt_deliverymanNote.length() < 6){
            this.edtxt_deliverymanNote.setError("password should be less than 160 letters");
            this.edtxt_deliverymanNote.requestFocus();
            return;
        }

        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        userKey = sharedPref.getString("userKey", "");
        userName = sharedPref.getString("name", "");
        userPhone = sharedPref.getString("phone", "");

        refUser = FirebaseDatabase.getInstance().getReference().child("User");
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

                            refUser.child(pack.getPackageOwnerId()).addListenerForSingleValueEvent(new ValueEventListener() {
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
            Toast.makeText(PickedPackageActivity.this, "SMS failed", Toast.LENGTH_LONG).show();
        }

    }

    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onClick(View view) {
        //update package status
//        refPackage.child(packKey).child("deliveryman").setValue(userKey);
//        refPackage.child(packKey).child("status").setValue(PACKAGE_STATUS_IN_PROCCESS);
//
//        //update deliveryman type
//        refUser.child(userKey).child("type").setValue(USER_TYPE_IN_PROCCESS);
//        //saved in the local memeory
//        SharedPreferences.Editor prefEditor = sharedPref.edit();
//        prefEditor.putString("type", String.valueOf(USER_TYPE_IN_PROCCESS));
//        prefEditor.commit();

    // send sms too package owner that theres a deliverman
        Toast.makeText(PickedPackageActivity.this, packageOwnerPhone, Toast.LENGTH_LONG).show();
        sendSms();      //        sendSMS(packageOwnerPhone, "Hi,\n"+ userName +" deliveryman wants to take your package:"+ packageId+"\nplease enter GetAndSend app and confirm the delivery!\nDeliveryman Note:"+edtxt_deliverymanNote.getText());
        startActivity(new Intent(PickedPackageActivity.this, MainActivity .class));
        finish();
        }
    }

