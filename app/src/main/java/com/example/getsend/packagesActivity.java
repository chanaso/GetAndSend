package com.example.getsend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.FirebaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class packagesActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ListView listView;
    private SharedPreferences sharedPref;
    private String userName, phone, rate, userKey;
    DatabaseReference refUser, refPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packages);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        listView = (ListView) findViewById(R.id.listView);
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        refUser = FirebaseDatabase.getInstance().getReference().child("User");
        refPackage = FirebaseDatabase.getInstance().getReference().child("Package");

        //getting the current username from the sp
        userName = sharedPref.getString("name", "");
        phone = sharedPref.getString("phone", "");
        rate = sharedPref.getString("rate", "");
        userKey = sharedPref.getString("userKey", "");
        extractUserPackages(userKey);

//        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(packagesActivity.this,
//                android.R.layout.simple_list_item_1,
//                new String[]{"g", "h"});
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = new Intent(packagesActivity.this, ListActivity.class);
//                intent.putExtra("CountryName", listView.getItemAtPosition(i).toString());
//                startActivity(intent);
//            }
//        });
//        listView.setAdapter(mAdapter);
    }

    //extract packages id for current user
    public void extractUserPackages(String userKey) {
        refUser.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot datas : dataSnapshot.getChildren()) {
                        String userPackages = datas.child("packages").getValue().toString();
                        String[] userPackagesList = userPackages.split(" ");
                        Toast.makeText(packagesActivity.this,"fvdc"+userPackagesList,Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
