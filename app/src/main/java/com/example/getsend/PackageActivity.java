package com.example.getsend;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

public class PackageActivity extends AppCompatActivity{
    private final String MY_PACKAGE_LIST = "myPackages", PACKAGE_LIST_TO_DELIVER = "packagesToDeliver", DELIMITER = "@";
    private final int OWNER = 1, DELIVERYMAN = 0;
    private TextView edtxt_Size, edtxt_Weight, edtxt_Location, edtxt_Destination, edtxt_delivery, edtxt_Status, edtxt_PackageId;
    private Package pack;
    private Button btn_1, btn_2, btn_confirm;
    private User currUser, user2;
    private SharedPreferences sharedPref;
    private String userKey, packKey, profileView, user2Key, user2Name, user2Rate, user2Id;
    private DatabaseReference refPackage, refUser;
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_package);


            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
            refPackage = FirebaseDatabase.getInstance().getReference().child("Package");
            refUser = FirebaseDatabase.getInstance().getReference().child("User");

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
                packKey = mBundle.getString("packageKey");

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
            edtxt_Status = findViewById(R.id.edtxt_StatusID);
            edtxt_Status.setText(pack.getStatus());

            if(currUser.getType() == OWNER) {
                //get the deliveryman details from DB
                refUser.child(pack.getDeliveryman()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            user2 = dataSnapshot.getValue(User.class);
                            saveUser2();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(PackageActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
                    }
                });
                switch (pack.getStatus()) {
                    case "Waiting for delivery":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setVisibility(View.INVISIBLE);
                        btn_confirm.setText("Delete Package");
                        btn_confirm.setOnClickListener(v -> {
                            //Delete package option
                            deleteCurrPackage(currUser, userKey, MY_PACKAGE_LIST);
                            Toast.makeText(PackageActivity.this, R.string.delete_package, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(PackageActivity.this, NavbarPackagesActivity.class));
                            finish();
                        });
                        break;
                    case "Waiting for approval":
                        btn_1.setText("View Deliveryman Details");
                        btn_1.setOnClickListener(v -> {
                            //View deliveryman details
                            viewUserDetails(pack.getDeliveryman());
                        });
                        btn_2.setText("Reject Deliveryman");
                        btn_2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Reject delivery
                                rejectDeliveryman(pack.getDeliveryman());
                                //send SMS to deliveryman that the delivery rejected
                                sendSms(user2.getPhone(), user2.getName() + " Deliveryman,\nPackage number: "+pack.getPackageId()+ " Rejected by the owner.");
                                Toast.makeText(PackageActivity.this, R.string.reject_package, Toast.LENGTH_LONG).show();
                                startActivity(new Intent(PackageActivity.this, NavbarPackagesActivity.class));
                                finish();
                            }
                        });
                        btn_confirm.setText(" Approve Delivery! ");
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Approve delivery
                                signPOA();
                                finish();
                            }
                        });
                        break;
                    case "On the way...":
                        btn_1.setText("View Deliveryman Details");
                        btn_1.setOnClickListener(v -> {
                            //View deliveryman details
                            viewUserDetails(pack.getDeliveryman());
                        });
                        btn_2.setText(" Open chat ");
                        btn_2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Open chat
                                openChat();
                            }
                        });
                        btn_confirm.setText(" Delivery Confirmation ");
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Delivery confirmation
                                refPackage.child(packKey).child("status").setValue("Arrived");
                                rateUser(user2, pack.getDeliveryman());
                            }
                        });
                        break;
                    case "Arrived":
                    case "Arrived :)":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setVisibility(View.INVISIBLE);
                        btn_confirm.setVisibility(View.INVISIBLE);
                        break;
                }
            }
            else{ //DELIVERYMAN type
                //get the owner details from DB
                refUser.child(pack.getPackageOwnerId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            user2 = dataSnapshot.getValue(User.class);
                            saveUser2();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(PackageActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
                    }
                });
                switch (pack.getStatus()) {
//                    case "Waiting for delivery":
//                        btn_1.setVisibility(View.INVISIBLE);
//                        btn_2.setText("View Owner Details");
//                        btn_2.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                //View owner details
//                                viewUserDetails(pack.getPackageOwnerId());
//                            }
//                        });
//                        btn_confirm.setVisibility(View.INVISIBLE);
//                        break;
                    case "Arrived :)":
                    case "Waiting for approval":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setVisibility(View.INVISIBLE);
                        btn_confirm.setVisibility(View.INVISIBLE);
                        break;
                    case "On the way...":
                        btn_1.setText("View Package Power Of Attorney");
                        btn_1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //view POA
                                openPOAView();
                            }
                        });
                        btn_2.setText(" Open chat ");
                        btn_2.setOnClickListener(v -> {
                            //Open chat
                            openChat();

                        });
                        btn_confirm.setText(" Delivery Confirmation ");
                        btn_confirm.setOnClickListener(v -> {
                            //Delivery confirmation
                            Toast.makeText(PackageActivity.this, "The owner didn't confirm the arrival yet", Toast.LENGTH_LONG).show();
                        });
                        break;
                    case "Arrived":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setVisibility(View.INVISIBLE);
                        btn_confirm.setText(" Delivery Confirmation ");
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Delivery confirmation
                                //Clean up before close the package delivery
                                cleanUpDelivery(pack.getPackageOwnerId());
                                //change the package status and rate
                                refPackage.child(packKey).child("status").setValue("Arrived :)");
                                btn_confirm.setVisibility(View.INVISIBLE);
                                rateUser(user2, pack.getPackageOwnerId());
                            }
                        });

                        break;
                }
             }
    }
    public void sendSms(String phone, String message) {
        if(checkPermission(Manifest.permission.SEND_SMS)){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null,message,null , null);
            Toast.makeText(PackageActivity.this, R.string.sms_send, Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(PackageActivity.this, R.string.sms_did_not_send, Toast.LENGTH_LONG).show();
        }

    }

    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    private void openChat() {
        String chatDetails = user2.getName() +DELIMITER+ packKey;
        Intent intent = new Intent(PackageActivity.this, ChatActivity.class);
        intent.putExtra("chat", chatDetails);
        startActivity(intent);
    }

    private void saveUser2() {
        //register user phone & password correct
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        // save user to the local memory
        Gson gson = new Gson();
        String json = gson.toJson(user2);
        prefEditor.putString("user2", json);
        prefEditor.commit();
    }
    private void deleteCurrPackage(User tmpUser, String userTmpKey, String package_list_type){
        tmpUser.deletePackage(package_list_type,packKey, userTmpKey);
        refPackage.child(packKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue();
                Toast.makeText(PackageActivity.this, R.string.delete_package, Toast.LENGTH_LONG).show();
                startActivity(new Intent(PackageActivity.this, NavbarPackagesActivity.class));
                finish();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PackageActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void viewUserDetails(String userViewKey){
        Gson gson = new Gson();
        String json = sharedPref.getString("user2", "");
        user2 = gson.fromJson(json, User.class);
        profileView = user2.getName() +DELIMITER+ user2.getRate();
        Intent intent = new Intent(PackageActivity.this, UserProfileViewActivity.class);
        intent.putExtra("profileView", profileView);
        startActivity(intent);
    }
    private void rejectDeliveryman(String deliverymanKey){
        //delete the package from the deliveryman list
        user2.deletePackage(PACKAGE_LIST_TO_DELIVER, packKey, deliverymanKey);
        //delete the deliveryman from the package details
        refPackage.child(packKey).child("deliveryman").setValue("");
        refPackage.child(packKey).child("status").setValue("Waiting for delivery");
    }
    private void signPOA(){
        Intent intent = new Intent(PackageActivity.this, PowerOfAttorney.class);
        // transfer the current package
        Gson gson = new Gson();
        String jsonPackage = gson.toJson(pack);
        intent.putExtra("package", jsonPackage);
        intent.putExtra("packageKey", packKey);
        startActivity(intent);
        finish();
    }
    private void rateUser(User user, String userKey){
        Intent intent = new Intent(PackageActivity.this, RateUserViewActivity.class);
        // transfer the selected user as json to packageActivity which will display that package
        String userDetails = user.getName()+DELIMITER+ userKey + DELIMITER + user.getRate() + DELIMITER + user.getNumOfRates();
        intent.putExtra("userToRate", userDetails);
        startActivity(intent);
        finish();
    }

    private void openPOAView(){
        Intent intent = new Intent(PackageActivity.this, PowerOfAttorneyView.class);
        // transfer the current package
        Gson gson = new Gson();
        String jsonPackage = gson.toJson(pack);
        intent.putExtra("package", jsonPackage);
        intent.putExtra("packageKey", packKey);
        startActivity(intent);
    }

    private void cleanUpDelivery(String userKey){
        //delete chat room from database
        DatabaseReference refChat = FirebaseDatabase.getInstance().getReference().child("ChatRooms");
        refChat.child("observable-"+packKey).removeValue();

        //delete signature from storage
        StorageReference signatureRef = FirebaseStorage.getInstance().getReference("Signatures/"+ userKey + ".JPEG");

        // Delete the file
        signatureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PackageActivity.this, "signature deleted successfully "+userKey, Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(PackageActivity.this, "Failed connect to storage "+userKey, Toast.LENGTH_LONG).show();
            }
        });

    }

    //handle device back button
    @Override
    public void onBackPressed() {
        startActivity(new Intent(PackageActivity.this, NavbarPackagesActivity.class));
        finish();
    }

}
