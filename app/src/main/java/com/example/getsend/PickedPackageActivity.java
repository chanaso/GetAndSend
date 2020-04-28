package com.example.getsend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.net.*;
import java.io.*;


public class PickedPackageActivity extends AppCompatActivity implements View.OnClickListener {

    private String location, packageId, userKey, userName, userPhone, packKey, packageOwnerPhone, packageOwnerId;
    private static final String PACKAGE_STATUS_IN_PROCCESS = "In proccess!";
    private static final int USER_TYPE_DELIVERYMAN_IN_PROCCESS = 2, USER_TYPE_DELIVERY_GETTER_IN_PROCCESS = 3;
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
                            refUser.child(packageOwnerId).addListenerForSingleValueEvent(new ValueEventListener() {
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
        String request = "https://sendpk.com/api/sms.php?username=972533917843&password=2JFUWsbE5Tbu@H&mobile=972549448461&sender=GetAndSend&message="+"Hi,\n"+ userName +" Deliveryman wants to take your package:"+ packageId+"\nplease confirm the delivery!";
        try
        {
            URL url = new URL(request);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection connection = null;
            if(urlConnection instanceof HttpURLConnection)
            {
                connection = (HttpURLConnection) urlConnection;
            }
            else
            {
                Toast.makeText(PickedPackageActivity.this, "Please enter an HTTP URL.", Toast.LENGTH_LONG).show();

            }
            final BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String urlString = "";
            String current;

            while((current = rd.readLine()) != null)
            {
                urlString += current;
            }
            Toast.makeText(PickedPackageActivity.this, urlString, Toast.LENGTH_LONG).show();
        }catch(IOException e)
        {
            Toast.makeText(PickedPackageActivity.this, "SMS NOT WORKING" + e, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        //update package status
        refPackage.child(packKey).child("deliveryman").setValue(userKey);
        refPackage.child(packKey).child("status").setValue(PACKAGE_STATUS_IN_PROCCESS);

        //update deliveryman type
        refUser.child(userKey).child("type").setValue(USER_TYPE_DELIVERYMAN_IN_PROCCESS);
        //update owner type
        refUser.child(packageOwnerId).child("type").setValue(USER_TYPE_DELIVERY_GETTER_IN_PROCCESS);
        //saved in the local memory
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("type", String.valueOf(USER_TYPE_DELIVERYMAN_IN_PROCCESS));
        
        prefEditor.commit();

    // send sms too package owner that theres a deliverman
        Toast.makeText(PickedPackageActivity.this, packageOwnerPhone, Toast.LENGTH_LONG).show();
        sendSms();
        startActivity(new Intent(PickedPackageActivity.this, MainActivity .class));
        finish();
        }
    }

