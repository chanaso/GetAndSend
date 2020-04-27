package com.example.getsend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;


public class PickedPackageActivity extends AppCompatActivity implements View.OnClickListener {
    private String location, packageId, userKey, userName, userPhone, packKey, packageOwnerPhone;
    private static final String PACKAGE_STATUS_IN_PROCCESS = "In proccess!";
    private static final int USER_TYPE_IN_PROCCESS = 2;
    // Find your Account Sid and Token at twilio.com/console
    // DANGER! This is insecure. See http://twil.io/secure
    public static final String ACCOUNT_SID = "AC7459f27f90947fc0b469094a4992f6e4";
    public static final String AUTH_TOKEN = "8681e4f34758edc493e7a9202a731953";


    private TextView edtxt_Size, edtxt_Weight, edtxt_Location, edtxt_Destination, edtxt_PackageId, edtxt_packageOwner;
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

        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        userKey = sharedPref.getString("userKey", "");
        userName = sharedPref.getString("name", "");
        userPhone = sharedPref.getString("phone", "");

        refUser = FirebaseDatabase.getInstance().getReference().child("User");
        refPackage = FirebaseDatabase.getInstance().getReference().child("Package");

        setTxtViews();
        btn_confirmDelivery.setOnClickListener(this);

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

    @Override
    public void onClick(View view) {
        //update package status
        refPackage.child(packKey).child("deliveryman").setValue(userKey);
        refPackage.child(packKey).child("status").setValue(PACKAGE_STATUS_IN_PROCCESS);

        //update deliveryman type
        refUser.child(userKey).child("type").setValue(USER_TYPE_IN_PROCCESS);
        //saved in the local memeory
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("type", String.valueOf(USER_TYPE_IN_PROCCESS));
        prefEditor.commit();

    // send sms too package owner that theres a deliverman
    // Install the Java helper library from twilio.com/docs/java/install

    Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    Message message = Message.creator(
            new com.twilio.type.PhoneNumber(packageOwnerPhone),
            new com.twilio.type.PhoneNumber(userPhone),
            userName+" deliveryman wants to take your package"+"\n delivery message:")
            .create();
//
    startActivity(new Intent(PickedPackageActivity.this, MainActivity .class));

    finish();
    }
    }

