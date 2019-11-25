package com.example.getsend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.core.view.View;

import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    EditText userName, phoneNumber, pass, passCon;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mcallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        userName = (EditText)findViewById(R.id.userNameID);
        phoneNumber = (EditText)findViewById(R.id.phoneNumberID);
        pass = (EditText)findViewById(R.id.passID);
        passCon = (EditText)findViewById(R.id.passConID);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnSignUpID).setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            //handle the already user
        }
    }

    private void registerUser(){
        String name = userName.getText().toString().trim();
        String phone = phoneNumber.getText().toString().trim();
        String pass = this.pass.getText().toString().trim();
        String passCon = this.passCon.getText().toString().trim();

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

        if(pass.isEmpty()){
            this.pass.setError("password required");
        }
    }


}
