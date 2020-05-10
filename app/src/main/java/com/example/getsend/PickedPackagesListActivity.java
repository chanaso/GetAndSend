package com.example.getsend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PickedPackagesListActivity extends AppCompatActivity {
    private String pickedPackageLocation, userKey;
    private DatabaseReference refPackage;
    private ListView listView_pickedPackages;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> userPickedPackagesList = new ArrayList<String>();
    private SharedPreferences sharedPref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_packages_picked);

        listView_pickedPackages = findViewById(R.id.listView_pickedPackagesID);
        refPackage = FirebaseDatabase.getInstance().getReference().child("Package");

        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        userKey = sharedPref.getString("userKey", "");

        mAdapter = new ArrayAdapter<String>(PickedPackagesListActivity.this,
                android.R.layout.simple_list_item_1,
                userPickedPackagesList);
        listView_pickedPackages.setAdapter(mAdapter);
        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            pickedPackageLocation = mBundle.getString("packageLocation");
            displayPackages(pickedPackageLocation);
            getIntent().removeExtra("showMessage");
        }

        listView_pickedPackages.setOnItemClickListener((adapterView, view, i, l) -> {
            if(i != 0) {
                Intent intent = new Intent(PickedPackagesListActivity.this, PickedPackageActivity.class);
                // transfer the selected package as json to packageActivity which will dispaly that package
                String pickedPackage = listView_pickedPackages.getItemAtPosition(0).toString().split("~")[1] +"@"+ listView_pickedPackages.getItemAtPosition(i).toString();
                intent.putExtra("pickedPackage", pickedPackage);
                startActivity(intent);
                finish();
            }
        });
    }

    private void displayPackages(String pickedPackageLocation) {
        refPackage.orderByChild("geoLocation").equalTo(pickedPackageLocation).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot datas : dataSnapshot.getChildren()) {
                        Package pack = datas.getValue(Package.class);
                        if (userPickedPackagesList.isEmpty()) {
                            userPickedPackagesList.add("~" + pack.getLocation() + "~");
                        }
                        if(pack.getDeliveryman().isEmpty() && !pack.getPackageOwnerId().equals(userKey)) {
                            userPickedPackagesList.add(pack.getPackageId());
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //handle device back button
    @Override
    public void onBackPressed() {
        startActivity(new Intent(PickedPackagesListActivity.this, JoinAsDeliverymanActivity.class));
        finish();
    }

}
