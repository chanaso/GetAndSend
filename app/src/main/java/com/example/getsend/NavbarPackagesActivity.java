package com.example.getsend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class NavbarPackagesActivity extends AppCompatActivity {
    private ListView listView_packages;
    private User currUser;
    private SharedPreferences sharedPref;
    private String userKey;
    private ArrayList<String> userPackagesList = new ArrayList<String>();
    private List<Package> packagesOfCurrUser =  new ArrayList<Package>();
    private List<String> packagesKeysOfCurrUser =  new ArrayList<String>();
    private DatabaseReference refPackage;
    private static final int USER_TYPE_DELIVERYMAN = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packages);
        listView_packages = findViewById(R.id.listView_packagesID);

        // store from local memory the current user
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("currUser", "");
        currUser = gson.fromJson(json, User.class);
        userKey = sharedPref.getString("userKey", "");

        refPackage = FirebaseDatabase.getInstance().getReference().child("Package");

        //extract user packages and display in the viewlist
        extractUserPackages();

        listView_packages.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(NavbarPackagesActivity.this, PackageActivity.class);
            // transfer the selected package as json to packageActivity which will dispaly that package
            // checking what the location of the selected package and transfer all the package details
            String jsonPackage = gson.toJson(packagesOfCurrUser.stream().
                    filter(p -> (p.getPackageId()+" "+p.getLocation()).equals(listView_packages.getItemAtPosition(i).toString().split("\n")[0])).
                    findAny().orElse(null));
            intent.putExtra("package", jsonPackage);
            intent.putExtra("packageKey", packagesKeysOfCurrUser.get(i));
            startActivity(intent);
        });
    }

    //extract packages id for current user and display them
    public void extractUserPackages() {
        String userPackages;
        //check user type to display the right packages
        if(currUser.getType() == USER_TYPE_DELIVERYMAN){
            userPackages = currUser.getPackagesToDeliver();
        }else {
            userPackages = currUser.getMyPackages();
        }
        if(userPackages.equals(""))
        {
            // theres no packsges for the current user
            ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(NavbarPackagesActivity.this,
            android.R.layout.simple_list_item_1,
            new String[]{"No packages history"});
            listView_packages.setAdapter(mAdapter);
        }else{
            // dispaly current user packages
            String[] userPackagesIdList = userPackages.split(" ");
            ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(NavbarPackagesActivity.this,
                    android.R.layout.simple_list_item_1,
                    userPackagesList);
            listView_packages.setAdapter(mAdapter);
            for (String index : userPackagesIdList) {
                if (!index.equals("")) {
                    refPackage.child(index).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Package pack = dataSnapshot.getValue(Package.class);
                                packagesOfCurrUser.add(pack);
                                packagesKeysOfCurrUser.add(dataSnapshot.getKey());
                                userPackagesList.add(pack.getPackageId() + " " + pack.getLocation() + "\n" + pack.getStatus());
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(NavbarPackagesActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }
    }
}
