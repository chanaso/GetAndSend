package com.example.getsend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    EditText userName, email, phoneNumber, pass, passCon;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;

//    private FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        userName = (EditText)findViewById(R.id.userID);
        email = (EditText)findViewById(R.id.emailID);
        phoneNumber = (EditText)findViewById(R.id.phoneNumberID);
        pass = (EditText)findViewById(R.id.passID);
        passCon = (EditText)findViewById(R.id.passConID);
        progressBar = findViewById(R.id.progressBarID);
        ref = FirebaseDatabase.getInstance().getReference().child("user");
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnSignUpID).setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }

    public static boolean isValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    private void registerUser(){
        final String name = userName.getText().toString().trim();
        final String phone = phoneNumber.getText().toString().trim();
        final String email = this.email.getText().toString().trim();
        final String pass = this.pass.getText().toString().trim();
        final String passCon = this.passCon.getText().toString().trim();

        if(name.isEmpty()){
            userName.setError("user name required");
            userName.requestFocus();
            return;
        }

        if(phone.isEmpty()){
            phoneNumber.setError("phone number required");
            phoneNumber.requestFocus();
            return;
        }

        if(phone.length() != 10){
            phoneNumber.setError("please enter a valid phone number");
            phoneNumber.requestFocus();
            return;
        }

        if(email.isEmpty()){
            this.email.setError("email required");
            this.email.requestFocus();
            return;
        }

        if(!isValid(email)){
            this.email.setError("illegal email");
            this.email.requestFocus();
            return;
        }

        if(pass.length() < 6){
            this.pass.setError("password should be at least 6 numbers");
            this.pass.requestFocus();
            return;
        }

        if(pass.isEmpty()){
            this.pass.setError("password required");
            this.pass.requestFocus();
            return;
        }

        if(!pass.equals(passCon))
        {
            this.pass.setError("passwords not the same");
            this.pass.requestFocus();
            return;
        }

        User user = new User(name, email, phone, pass);
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            final User user = new User(name, email, phone, pass);

                            FirebaseDatabase.getInstance().getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        ref.orderByChild("phone").equalTo(user.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    Toast.makeText(SignUpActivity.this, "user phone number already exist", Toast.LENGTH_LONG).show();
                                                }
                                                else {
//                                                    ref.push().setValue(user);
                                                    Toast.makeText(SignUpActivity.this, "registration success", Toast.LENGTH_LONG).show();
                                                    FirebaseUser currUser = mAuth.getCurrentUser();
                                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                }
                                            }

                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        Toast.makeText(SignUpActivity.this, "registration success",Toast.LENGTH_LONG).show();
                                    }else {
                                        //failure
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(android.view.View v) {
        switch (v.getId()){
            case R.id.btnSignUpID:
                registerUser();
                break;
        }
    }
}
