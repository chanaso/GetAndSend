package com.example.getsend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    EditText edtxtPhone;
    EditText edtxtPassword;
    private FirebaseAuth mAuth;
    SharedPreferences sharedPref;
    private DatabaseReference ref;
    private CountryCodePicker ccp;

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
        sharedPref = getSharedPreferences("data",MODE_PRIVATE);
        ref = FirebaseDatabase.getInstance().getReference().child("User");


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLogInID:
            {
                final String prePhone = ccp.getSelectedCountryCode();
                final String phone = "+" + prePhone + edtxtPhone.getText().toString().trim();
                String pass = edtxtPassword.getText().toString().trim();
                userLogin();
                ref.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                    String value;
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null){
                            //it means user already registered
                            for(DataSnapshot data: dataSnapshot.getChildren()) {
                                value=data.child("pass").getValue().toString();
                            }
                            if(value.equals(pass)){
                                //register user phone & password correct
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    SharedPreferences.Editor prefEditor = sharedPref.edit();
                                    prefEditor.putInt("isLogged",1);
                                    prefEditor.commit();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
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


//                mAuth.signInWithEmailAndPassword(email, pass)
//                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//                                    // Sign in success, update UI with the signed-in user's information
////                                    Log.d(TAG, "signInWithEmail:success");
//                                    FirebaseUser user = mAuth.getCurrentUser();
//                                    SharedPreferences.Editor prefEditor = sharedPref.edit();
//                                    prefEditor.putInt("isLogged",1);
//                                    prefEditor.commit();
//                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
//
//                                } else {
//                                    // If sign in fails, display a message to the user.
////                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
//                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//
//                                // ...
//                            }
//                        });
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

    public void userLogin() {
        String phone = edtxtPhone.getText().toString().trim();
        String password = edtxtPassword.getText().toString().trim();
        if (phone.isEmpty()) {
            edtxtPhone.setError("phone number required");
            edtxtPhone.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtxtPassword.setError("password required");
            edtxtPassword.requestFocus();
            return;
        }
    }

}
