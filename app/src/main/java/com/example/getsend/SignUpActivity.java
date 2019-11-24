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

public class SignUpActivity extends AppCompatActivity {

    FirebaseAuth auth;
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
        auth = FirebaseAuth.getInstance();

        mcallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
            }
        };
    }

    public void send_sms(View v){
        String number = phoneNumber.getText().toString();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number, 60, TimeUnit.SECONDS, this, mcallback
        );
    }

    public void sign_in_phone(PhoneAuthCredential credential){
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "user signed in successfuly", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
