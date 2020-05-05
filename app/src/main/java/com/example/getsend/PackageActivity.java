package com.example.getsend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class PackageActivity extends AppCompatActivity{
    private final String MY_PACKAGE_LIST = "myPackages", PACKAGE_LIST_TO_DELIVER = "packagesToDeliver";
    private final int OWNER = 1, DELIVERYMAN = 0;
    private TextView edtxt_Size, edtxt_Weight, edtxt_Location, edtxt_Destination, edtxt_delivery, edtxt_Status, edtxt_PackageId;
    private Package pack;
    private Button btn_1, btn_2, btn_confirm;
    private User currUser, user2;
    private SharedPreferences sharedPref;
    private String userKey, packKey, profileView, user2Key, user2Name, user2Rate, user2Id;
    private DatabaseReference refPackage, refUser;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_package);

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
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Delete package option
                                deleteCurrPackage(currUser, userKey, MY_PACKAGE_LIST);
                            }
                        });
                        break;
                    case "Waiting for approval":
                        btn_1.setText("View Deliveryman Details");
                        btn_1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //View deliveryman details
                                viewUserDetails(pack.getDeliveryman());
                            }
                        });
                        btn_2.setText("Reject Deliveryman");
                        btn_2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Reject delivery
                                rejectDeliveryman(pack.getDeliveryman());
                                //ToDo
                                //send SMS to deliveryman that the delivery was rejected
                            }
                        });
                        btn_confirm.setText(" Approve Delivery! ");
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Approve delivery
                                signPOA();
                            }
                        });
                        break;
                    case "On the way...":
                        btn_1.setVisibility(View.INVISIBLE);
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
                                rateUser(user2, pack.getDeliveryman());
                                refPackage.child(packKey).child("status").setValue("Arrived :)");
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
                    case "Waiting for delivery":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setText("View Owner Details");
                        btn_2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //View owner details
                                viewUserDetails(pack.getPackageOwnerId());
                            }
                        });
                        btn_confirm.setVisibility(View.INVISIBLE);
                        break;
                    case "Waiting for approval":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setVisibility(View.INVISIBLE);
                        btn_confirm.setVisibility(View.INVISIBLE);
                        break;
                    case "On the way...":
                        btn_2.setText("View Package Power Of Attorney");
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
                    case "Arrived :)":
                        btn_1.setVisibility(View.INVISIBLE);
                        btn_2.setVisibility(View.INVISIBLE);
                        btn_confirm.setText(" Delivery Confirmation ");
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Delivery confirmation
                                rateUser(user2, pack.getPackageOwnerId());
                                btn_confirm.setVisibility(View.INVISIBLE);
                            }
                        });
                        break;
                }
             }
    }

    private void openChat() {
        String chatDetails = user2.getName() +"@"+ packKey;
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
        profileView = user2.getName() +"@"+ user2.getRate();
        Intent intent = new Intent(PackageActivity.this, UserProfileViewActivity.class);
        intent.putExtra("profileView", profileView);
        startActivity(intent);
    }
    private void rejectDeliveryman(String deliverymanKey){
        deleteCurrPackage(user2, deliverymanKey, PACKAGE_LIST_TO_DELIVER);
    }
    private void signPOA(){
        Intent intent = new Intent(PackageActivity.this, PowerOfAttorney.class);
        // transfer the current package
        Gson gson = new Gson();
        String jsonPackage = gson.toJson(pack);
        intent.putExtra("package", jsonPackage);
        intent.putExtra("packageKey", packKey);
        startActivity(intent);
    }
    private void rateUser(User user, String userKey){
        Intent intent = new Intent(PackageActivity.this, RateUserViewActivity.class);
        // transfer the selected user as json to packageActivity which will dispaly that package
        String userDetails = user.getName()+"@"+ userKey + "@" + user.getRate() + "@" + user.getNumOfRates();
        intent.putExtra("userToRate", userDetails);
        startActivity(intent);
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

}
