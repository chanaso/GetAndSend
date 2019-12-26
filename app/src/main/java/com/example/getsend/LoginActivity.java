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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    EditText edtxtPhone;
    EditText edtxtPassword;
    private FirebaseAuth mAuth;
    SharedPreferences sharedPref;
    private DatabaseReference ref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtxtPhone = findViewById(R.id.phoneID);
        edtxtPassword = findViewById(R.id.passID);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnLogInID).setOnClickListener(this);
        findViewById(R.id.txtCreateAccountID).setOnClickListener(this);
        sharedPref = getSharedPreferences("data",MODE_PRIVATE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLogInID:
            {
                String phone = edtxtPhone.getText().toString().trim();
                String pass = edtxtPassword.getText().toString().trim();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child("User").child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            // use "username" already exists
                            Toast.makeText(LoginActivity.this, "User exist!", Toast.LENGTH_SHORT).show();
                            // Let the user know he needs to pick another username.
                        } else {
                            // User does not exist. NOW call createUserWithEmailAndPassword
                            Toast.makeText(LoginActivity.this, "User not exist!", Toast.LENGTH_SHORT).show();

//                            mAuth.createUserWithPassword(...);
                            // Your previous code here.

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
        String userName = edtxtPhone.getText().toString().trim();
        String password = edtxtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "please enter User Name", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "please enter password", Toast.LENGTH_LONG).show();
            return;
        }

    }

}
