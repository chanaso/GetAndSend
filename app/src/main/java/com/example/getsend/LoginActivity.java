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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtxt_Phone, edtxt_Password;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPref;
    private DatabaseReference refUser;
    private CountryCodePicker ccp;
    private String userKey, password;
    private User currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ccp = findViewById(R.id.ccp);
        edtxt_Phone = findViewById(R.id.edtxt_PhoneID);
        edtxt_Password = findViewById(R.id.edtxt_PasswordID);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btn_LogInID).setOnClickListener(this);
        findViewById(R.id.txt_CreateAccountID).setOnClickListener(this);
        findViewById(R.id.txt_forgotPassID).setOnClickListener(this);

        sharedPref = getSharedPreferences("userDetails",MODE_PRIVATE);
        refUser = FirebaseDatabase.getInstance().getReference().child("User");
    }

    public void loginUser(){
        final String prePhone = ccp.getSelectedCountryCode();
        String phone1 = edtxt_Phone.getText().toString().trim();
        final String phone = "+" + prePhone + edtxt_Phone.getText().toString().trim();
        password = edtxt_Password.getText().toString().trim();

        //check inputs validation
        if (phone1.matches("")) {
            edtxt_Phone.setError(getString(R.string.require_phone));
            edtxt_Phone.requestFocus();
            return;
        }
        if (password.matches("")) {
            edtxt_Password.setError(getString(R.string.require_pass));
            edtxt_Password.requestFocus();
            return;
        }
        //check if the user is registered already and if the phone number exist in db
        refUser.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            String value;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null){
                    //it means user already registered
                    for(DataSnapshot datas: dataSnapshot.getChildren()) {
                        value = datas.child("pass").getValue().toString();
                        currUser = datas.getValue(User.class);
                        userKey = datas.getKey();
                    }
                    //check if the input password is correct
                    if(value.equals(password)){
                        saveCurrUser();
                    }
                    else{
                        //Incorrect password
                        edtxt_Password.setError("Incorrect password");
                        edtxt_Password.requestFocus();
                    }
                }
                else{
                    //It is new users
                    Toast.makeText(LoginActivity.this, R.string.User_does_not_exist, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_LogInID:
            {
                loginUser();
                break;
            }
            case R.id.txt_CreateAccountID:
            {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
                break;
            }
            case R.id.txt_forgotPassID:
            {
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
                finish();
                break;
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
