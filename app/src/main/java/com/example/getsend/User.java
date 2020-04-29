package com.example.getsend;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class User {
    private String name, phone, pass, packages;
    private int rate, type, id;
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
//    (2) USER_TYPE_DELIVERYMAN_IN_PROCCESS
//    (3) USER_TYPE_DELIVERY_GETTER_IN_PROCCESS

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

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    @Exclude
    //add the package key that added to the current user list of keys packages
    public void setPackages(String packageKey, String userKey) {
        refUser.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if(dataSnapshot.hasChild("packages")){
                        String userPackages = dataSnapshot.child("packages").getValue().toString();
                        //set the previous keys + the new package key
                        refUser.child(userKey).child("packages").setValue(userPackages + packageKey + DELIMITER);
                    }else {
                        refUser.child(userKey).child("packages").setValue(packageKey + DELIMITER);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User(String name, String phone, String pass) {
        this.name = name;
        this.phone = phone;
        this.pass = pass;
        this.rate = 0;
        this.type = -1;
        this.packages = "";
        this.id = 0;
    }
}
