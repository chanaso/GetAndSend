package com.example.getsend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtxtPhone;
    private EditText edtxtPassword;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPref;
    private DatabaseReference ref;
    private CountryCodePicker ccp;
    public static final String KEY_USER_NAME = "userName";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        edtxtPhone = findViewById(R.id.phoneID);
        edtxtPassword = findViewById(R.id.passID);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnLogInID).setOnClickListener(this);
        findViewById(R.id.txtCreateAccountID).setOnClickListener(this);
        sharedPref = getSharedPreferences("userName",MODE_PRIVATE);
        ref = FirebaseDatabase.getInstance().getReference().child("User");
    }

    public void loginUser(){
        final String prePhone = ccp.getSelectedCountryCode();
        final String phone = "+" + prePhone + edtxtPhone.getText().toString().trim();
        String pass = edtxtPassword.getText().toString().trim();
        integrityCheck();

        //check if the user is regiter already and if the phine number exist in db
        ref.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            String value;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userName = "", phoneNumber = "", rate = "";

                if (dataSnapshot.getValue() != null){
                    //it means user already registered
                    for(DataSnapshot data: dataSnapshot.getChildren()) {
                        value=data.child("pass").getValue().toString();
                        userName = data.child("name").getValue().toString();
                        phoneNumber = data.child("phone").getValue().toString();
                        rate = data.child("rate").getValue().toString();

                    }
                    //check if the input password is correct
                    if(value.equals(pass)){
                        //register user phone & password correct
                        // save the registered user and lead to the main activity
                        SharedPreferences.Editor prefEditor = sharedPref.edit();
                        prefEditor.putString("name",userName);
                        prefEditor.putString("phone", phoneNumber);
                        prefEditor.putString("rate", rate);
                        prefEditor.commit();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    else{
                        //wrong password
                        Toast.makeText(LoginActivity.this, "Inncorrect password", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    //It is new users
                    Toast.makeText(LoginActivity.this, "User not exist", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLogInID:
            {
               loginUser();
                break;
            }
            case R.id.txtCreateAccountID:
            {
                Intent in=new Intent(this,SignUpActivity.class);
                startActivity(in);
                break;
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        FirebaseAuth.getInstance().signOut(); // sign out user
    }

    public void integrityCheck() {
        String phone = edtxtPhone.getText().toString().trim();
        String password = edtxtPassword.getText().toString().trim();
        if (phone.matches("")) {
            edtxtPhone.setError("phone number required");
            edtxtPhone.requestFocus();
            return;
        }
        if (password.matches("")) {
            edtxtPassword.setError("password required");
            edtxtPassword.requestFocus();
            return;
        }
    }
}
