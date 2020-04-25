package com.example.getsend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class PackagesActivity extends AppCompatActivity {
    private ListView listView;
    private SharedPreferences sharedPref;
    private String userName, phone, rate, userKey, flag = "0";
    private ArrayList<String> userPackagesList = new ArrayList<String>();
    private List<Package> packagesOfCurrUser =  new ArrayList<Package>();


    DatabaseReference refUser, refPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packages);
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(PackagesActivity.this, PackageActivity.class);
                // transfer the selected package as json to packageActivity which will dispaly that package
                Gson gson = new Gson();
                String jsonPackage = gson.toJson(packagesOfCurrUser.stream().
                        filter(p -> p.getLocation().equals(listView.getItemAtPosition(i).toString().split("\n")[0])).
                        findAny().orElse(null));
                intent.putExtra("package", jsonPackage);
                startActivity(intent);
            }
        });
    }

    //extract packages id for current user
    public void extractUserPackages(String userKey) {
        refUser.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user  = dataSnapshot.getValue(User.class);
                    if(user.getPackages().equals(""))
                    {
                        // theres no packsges for the current user
                        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(PackagesActivity.this,
                        android.R.layout.simple_list_item_1,
                        new String[]{"No packages history"});
                        listView.setAdapter(mAdapter);
                    }else{

                        String userPackages = user.getPackages();
                        String[] userPackagesIdList = userPackages.split(" ");
                        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(PackagesActivity.this,
                                android.R.layout.simple_list_item_1,
                                userPackagesList);
                        listView.setAdapter(mAdapter);
                        for (String i : userPackagesIdList) {
                            refPackage.child(i).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshotP) {
                                    if (dataSnapshotP.exists()) {
                                        Package pack = dataSnapshotP.getValue(Package.class);
                                        packagesOfCurrUser.add(pack);
                                        userPackagesList.add(pack.getLocation()+"\n"+pack.getStatus());
                                        mAdapter.notifyDataSetChanged();
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
