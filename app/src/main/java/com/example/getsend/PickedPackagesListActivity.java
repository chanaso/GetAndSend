package com.example.getsend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PickedPackagesListActivity extends AppCompatActivity {
    private String pickedPackageLocation;
    private DatabaseReference refPackage;
    private ListView listView_pickedPackages;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> userPickedPackagesList = new ArrayList<String>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_packages_picked);

        listView_pickedPackages = findViewById(R.id.listView_pickedPackagesID);
        refPackage = FirebaseDatabase.getInstance().getReference().child("Package");

        mAdapter = new ArrayAdapter<String>(PickedPackagesListActivity.this,
                android.R.layout.simple_list_item_1,
                userPickedPackagesList);
        listView_pickedPackages.setAdapter(mAdapter);
        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            pickedPackageLocation = mBundle.getString("packageLocation");
//            Toast.makeText(PickedPackagesListActivity.this, pickedPackageLocation+"\n[35.1986715, 31.776654]",Toast.LENGTH_LONG).show();
            displayPackages(pickedPackageLocation);
            getIntent().removeExtra("showMessage");
        }

        listView_pickedPackages.setOnItemClickListener((adapterView, view, i, l) -> {
            if(i != 0) {
                Intent intent = new Intent(PickedPackagesListActivity.this, PickedPackageActivity.class);
                // transfer the selected package as json to packageActivity which will dispaly that package
                String pickedPackage = listView_pickedPackages.getItemAtPosition(0).toString().split("~")[1] +"@"+ listView_pickedPackages.getItemAtPosition(i).toString();
                Toast.makeText(PickedPackagesListActivity.this, pickedPackage, Toast.LENGTH_LONG).show();
                intent.putExtra("pickedPackage", pickedPackage);
                startActivity(intent);
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
                        if(userPickedPackagesList.isEmpty()){
                            userPickedPackagesList.add("~"+pack.getLocation()+"~");
                        }
                        userPickedPackagesList.add(pack.getPackageId());
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
