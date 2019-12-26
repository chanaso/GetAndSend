package com.example.getsend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText userName, phoneNumber, pass, passCon, verifiCode;
    private String codeSend;
//    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private CountryCodePicker ccp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        userName = (EditText)findViewById(R.id.userID);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        phoneNumber = (EditText)findViewById(R.id.phoneNumberID);
        verifiCode = (EditText)findViewById(R.id.verificationCodeID);
        pass = (EditText)findViewById(R.id.passID);
        passCon = (EditText)findViewById(R.id.passConID);
//        progressBar = findViewById(R.id.progressBarID);
        ref = FirebaseDatabase.getInstance().getReference().child("User");
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnSignUpID).setOnClickListener(this);
        findViewById(R.id.btnVerifyID).setOnClickListener(this);

    }

    private void sendVerificationCode() {

        final String name = userName.getText().toString().trim();
        final String prePhone = ccp.getSelectedCountryCode();
        final String phone = "+" + prePhone + phoneNumber.getText().toString().trim();
        Toast.makeText(SignUpActivity.this, phone, Toast.LENGTH_LONG).show();


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

        if(phone.length() != 13){
            phoneNumber.setError("please enter a valid phone number");
            phoneNumber.requestFocus();
            return;
        }

        ref.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() != null){
                //it means user already registered
                Toast.makeText(SignUpActivity.this, "User exist already", Toast.LENGTH_LONG).show();
                phoneNumber.requestFocus();
                return;
            }
            else{
                //It is new users
                PhoneAuthProvider.getInstance().verifyPhoneNumber(phone, 60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallbacks);        // OnVerificationStateChangedCallbacks
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Toast.makeText(SignUpActivity.this, "code success", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(SignUpActivity.this, "code faile", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSend = s;
            Toast.makeText(SignUpActivity.this, "sended", Toast.LENGTH_LONG).show();

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }


    private void registerUser(){
        final String name = userName.getText().toString().trim();
        final String prePhone = ccp.getSelectedCountryCode();
        final String phone = "+" + prePhone + phoneNumber.getText().toString().trim();
        final String code = verifiCode.getText().toString().trim();
        final String pass = this.pass.getText().toString().trim();
        final String passCon = this.passCon.getText().toString().trim();


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

        User user = new User(name, phone, pass);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSend, code);
        signInWithPhoneAuthCredential(credential, user);
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, User user) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            ref.push().setValue(user);
                            Toast.makeText(SignUpActivity.this, "registration success", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(SignUpActivity.this, "Inncorrect Verification Code", Toast.LENGTH_LONG).show();
                            }
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
            case R.id.btnVerifyID:
                sendVerificationCode();
        }
    }
}
