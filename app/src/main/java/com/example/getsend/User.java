package com.example.getsend;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class User {
    private String name, phone, pass, packagesToDeliver, myPackages, id;
    private int rate, type, numOfRates;
    private static final String DELIMITER = " ";
    private transient DatabaseReference refUser = FirebaseDatabase.getInstance().getReference().child("User");

    @Exclude
    public DatabaseReference getRefUser() {
        return refUser;
    }

//    types:
//    (-1) NOT SET
//    (0) USER_TYPE_DELIVERYMAN
//    (1) USER_TYPE_DELIVERY_GETTER

    public User(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setRate(int rate){ this.rate = rate; }

    public int getRate(){ return rate; }

    public int getType() {
        return type;
    }

    public String getPass() {
        return pass;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPackagesToDeliver() {
        return packagesToDeliver;
    }

    public void setPackagesToDeliver(String packageKey, String userKey) {
        setPackages("packagesToDeliver",packageKey, userKey);
        this.packagesToDeliver = this.packagesToDeliver + DELIMITER + packageKey ;
    }

    public String getMyPackages() {
        return myPackages;
    }

    public void setMyPackages(String packageKey, String userKey) {
        setPackages("myPackages",packageKey, userKey);
        this.myPackages = this.myPackages + DELIMITER + packageKey ;
    }

    @Exclude
    //add the package key that added to the current user list of keys packages
    public void setPackages(String packageType ,String packageKey, String userKey) {
        refUser.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if(dataSnapshot.hasChild(packageType)){
                        String userPackages = dataSnapshot.child(packageType).getValue().toString();
                        //set the previous keys + the new package key
                        refUser.child(userKey).child(packageType).setValue(userPackages + packageKey + DELIMITER);
                    }else {
                        refUser.child(userKey).child(packageType).setValue(packageKey + DELIMITER);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(getApplicationContext(), R.string.access_to_Firebase_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Exclude
    //delete the package key from the current user list of keys packages
    public void deletePackage(String packageType ,String packageKey, String userKey) {
        refUser.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if(dataSnapshot.hasChild(packageType)){
                        String userPackages = dataSnapshot.child(packageType).getValue().toString();
                        //set the previous keys - the package key
                        userPackages = userPackages.replace(" "+packageKey+" "," ");
                        refUser.child(userKey).child(packageType).setValue(userPackages);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumOfRates() {
        return numOfRates;
    }

    public void setNumOfRates(int numOfRates) {
        this.numOfRates = numOfRates;
    }

    public User(String name, String phone, String pass, String id) {
        this.name = name;
        this.phone = phone;
        this.pass = pass;
        this.rate = 0;
        this.type = -1;
        this.packagesToDeliver = "";
        this.myPackages = "";
        this.id = id;
        this.numOfRates = 0;
    }
}
