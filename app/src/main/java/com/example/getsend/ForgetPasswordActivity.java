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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
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

public class ForgetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtxt_phoneNumber, edtxt_pass, edtxt_passCon, edtxt_verifiCode;
    private Button btn_Enter, btn_Verify;
    private String codeSend, userKey;
    private FirebaseAuth mAuth;
    private DatabaseReference refUser;
    private CountryCodePicker edtxt_ccp;
    private SharedPreferences sharedPref;
    private User currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_paswword);

        btn_Enter = findViewById(R.id.btn_EnterID);
        btn_Verify = findViewById(R.id.btn_VerifyID);
        edtxt_ccp = findViewById(R.id.ccp);
        edtxt_phoneNumber = findViewById(R.id.edtxt_phoneNumberID);
        edtxt_verifiCode = findViewById(R.id.edtxt_verificationCodeID);
        edtxt_pass = findViewById(R.id.edtxt_passID);
        edtxt_passCon = findViewById(R.id.edtxt_passConID);
        refUser = FirebaseDatabase.getInstance().getReference().child("User");
        mAuth = FirebaseAuth.getInstance();

        btn_Enter.setOnClickListener(this);
        btn_Verify.setOnClickListener(this);

        sharedPref = getSharedPreferences("userDetails",MODE_PRIVATE);
    }

    private void sendVerificationCode() {
        final String prePhone = edtxt_ccp.getSelectedCountryCode();
        final String phone = "+" + prePhone + edtxt_phoneNumber.getText().toString().trim();

        // validation check
        if(phone.isEmpty()){
            edtxt_phoneNumber.setError("phone number required");
            edtxt_phoneNumber.requestFocus();
            return;
        }

        if(phone.length() != 13){
            edtxt_phoneNumber.setError("please enter a valid phone number");
            edtxt_phoneNumber.requestFocus();
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone, 60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    // send verification code to the entered number
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Toast.makeText(ForgetPasswordActivity.this, R.string.verification_code_failed_Too_many_tries, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(ForgetPasswordActivity.this, R.string.verification_code_failed_please_try_again, Toast.LENGTH_LONG).show();

        }
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSend = s;
            Toast.makeText(ForgetPasswordActivity.this, R.string.verification_code_sent, Toast.LENGTH_LONG).show();

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void signInUser(){
        final String prePhone = edtxt_ccp.getSelectedCountryCode();
        final String phone = "+" + prePhone + edtxt_phoneNumber.getText().toString().trim();
        final String code = edtxt_verifiCode.getText().toString().trim();
        final String pass = this.edtxt_pass.getText().toString().trim();
        final String passCon = this.edtxt_passCon.getText().toString().trim();

        // integrity input check
        if(pass.length() < 6){
            this.edtxt_pass.setError("password should be at least 6 numbers");
            this.edtxt_pass.requestFocus();
            return;
        }

        if(pass.matches("")){
            this.edtxt_pass.setError("password required");
            this.edtxt_pass.requestFocus();
            return;
        }

        if(code.matches("")){
            this.edtxt_verifiCode.setError("verification code required");
            this.edtxt_verifiCode.requestFocus();
            return;
        }

        if(!pass.equals(passCon))
        {
            this.edtxt_pass.setError("passwords not the same");
            this.edtxt_pass.requestFocus();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSend, code);
//        User user = new User(name, phone, pass, id);
        signInWithPhoneAuthCredential(credential, phone);
    }

    // sign up user and check if the input code is matched
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, String phone) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //set the new password
                        refUser.child(userKey).child("pass").setValue(edtxt_pass);
                        //check if the user is registered already and if the phone number exist in db
                        refUser.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.getValue() != null){
                                    for(DataSnapshot datas: dataSnapshot.getChildren()) {
                                        currUser = datas.getValue(User.class);
                                        userKey = datas.getKey();
                                    }
                                    saveCurrUser();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            Toast.makeText(ForgetPasswordActivity.this, "Inncorrect Verification Code", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveCurrUser() {
        //register user phone & password correct
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        // convert User object to json and
        // save the registered user to the local memory
        Gson gson = new Gson();
        String json = gson.toJson(currUser);
        prefEditor.putString("currUser", json);
        prefEditor.putString("userKey", userKey);
        prefEditor.commit();
        startActivity(new Intent(ForgetPasswordActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onClick(android.view.View v) {
        switch (v.getId()){
            case R.id.btn_EnterID:
                signInUser();
                break;
            case R.id.btn_VerifyID:
                sendVerificationCode();
                break;
        }
    }

    //handle device back button
    @Override
    public void onBackPressed() {
        startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));
        finish();
    }
}
