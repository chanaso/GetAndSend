package com.example.getsend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtxt_userName, edtxt_phoneNumber, edtxt_pass, edtxt_passCon, edtxt_verifiCode, edtxt_userId;
    private Button btn_SignUp, btn_Verify;
    private String codeSend, userKey;
    private FirebaseAuth mAuth;
    private DatabaseReference refUser;
    private CountryCodePicker edtxt_ccp;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btn_SignUp = findViewById(R.id.btn_SignUpID);
        btn_Verify = findViewById(R.id.btn_VerifyID);
        edtxt_userName = findViewById(R.id.edtxt_userNameID);
        edtxt_ccp = findViewById(R.id.ccp);
        edtxt_phoneNumber = findViewById(R.id.edtxt_phoneNumberID);
        edtxt_verifiCode = findViewById(R.id.edtxt_verificationCodeID);
        edtxt_pass = findViewById(R.id.edtxt_passID);
        edtxt_passCon = findViewById(R.id.edtxt_passConID);
        edtxt_userId = findViewById(R.id.edtxt_userIdID);
        refUser = FirebaseDatabase.getInstance().getReference().child("User");
        mAuth = FirebaseAuth.getInstance();

        btn_SignUp.setOnClickListener(this);
        btn_Verify.setOnClickListener(this);

        sharedPref = getSharedPreferences("userDetails",MODE_PRIVATE);
    }

    private void sendVerificationCode() {
        final String name = edtxt_userName.getText().toString().trim();
        final String prePhone = edtxt_ccp.getSelectedCountryCode();
        final String phone = "+" + prePhone + edtxt_phoneNumber.getText().toString().trim();

        // validation check
        if(name.isEmpty()){
            edtxt_userName.setError(getString(R.string.require_name));
            edtxt_userName.requestFocus();
            return;
        }

        if(phone.isEmpty()){
            edtxt_phoneNumber.setError(getString(R.string.require_phone));
            edtxt_phoneNumber.requestFocus();
            return;
        }

        if(phone.length() != 13){
            edtxt_phoneNumber.setError(getString(R.string.invalid_phone));
            edtxt_phoneNumber.requestFocus();
            return;
        }

        //  check if the user exist in the db
            refUser.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener(){
                @Override
                public void onDataChange(DataSnapshot dataSnapshot){
                    if(dataSnapshot.exists()) {
                        Toast.makeText(SignUpActivity.this, phone + " registered already", Toast.LENGTH_LONG).show();
                    }
                    else {
                        // send verifiction to new user name
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phone, 60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallbacks);        // OnVerificationStateChangedCallbacks
                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SignUpActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // send verification code to the entered number
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Toast.makeText(SignUpActivity.this, R.string.verification_code_failed_Too_many_tries, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(SignUpActivity.this, R.string.verification_code_failed_please_try_again, Toast.LENGTH_LONG).show();

        }
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSend = s;
            Toast.makeText(SignUpActivity.this, R.string.verification_code_sent, Toast.LENGTH_LONG).show();

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void registerUser(){
        final String name = edtxt_userName.getText().toString().trim();
        final String prePhone = edtxt_ccp.getSelectedCountryCode();
        final String phone = "+" + prePhone + edtxt_phoneNumber.getText().toString().trim();
        final String code = edtxt_verifiCode.getText().toString().trim();
        final String pass = this.edtxt_pass.getText().toString().trim();
        final String passCon = this.edtxt_passCon.getText().toString().trim();
        final String id = edtxt_userId.getText().toString().trim();

        // integrity input check
        if(pass.length() < 6){
            this.edtxt_pass.setError(getString(R.string.invalid_pass));
            this.edtxt_pass.requestFocus();
            return;
        }

        if(pass.matches("")){
            this.edtxt_pass.setError(getString(R.string.require_pass));
            this.edtxt_pass.requestFocus();
            return;
        }

        if(code.matches("")){
            this.edtxt_verifiCode.setError(getString(R.string.require_validation));
            this.edtxt_verifiCode.requestFocus();
            return;
        }

        if(!pass.equals(passCon))
        {
            this.edtxt_pass.setError(getString(R.string.same_pass));
            this.edtxt_pass.requestFocus();
            return;
        }

        if(id.length() != 9)
        {
            this.edtxt_userId.setError(getString(R.string.incorrect_id));
            this.edtxt_userId.requestFocus();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSend, code);
        User user = new User(name, phone, pass, id);
        signInWithPhoneAuthCredential(credential, user);
    }

    // sign up user and check if the input code is matched
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, User user) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //matched input code and push user details to db
                        //get user id
                        DatabaseReference ref = refUser.push();
                        userKey = ref.getKey();
                        ref.setValue(user);
                        Toast.makeText(SignUpActivity.this, getString(R.string.user_added), Toast.LENGTH_LONG).show();
                        // saving the user that registered to local memory.
                        SharedPreferences.Editor prefEditor = sharedPref.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(user);
                        prefEditor.putString("currUser", json);
                        prefEditor.putString("userKey", userKey);
                        prefEditor.commit();
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        finish();
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            Toast.makeText(SignUpActivity.this,getString(R.string.incorrect_validation), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(android.view.View v) {
        switch (v.getId()){
            case R.id.btn_SignUpID:
                registerUser();
                break;
            case R.id.btn_VerifyID:
                sendVerificationCode();
                break;
        }
    }

    //handle device back button
    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    }

}
