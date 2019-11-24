package com.example.getsend;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnLogIn;
    TextView txtCreatAccount;
    EditText edtxtUserName;
    EditText edtxtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogIn = findViewById(R.id.btnLogInID);
        txtCreatAccount = findViewById(R.id.txtCreateAccountID);
        edtxtUserName = findViewById(R.id.userNameID);
        edtxtPassword = findViewById(R.id.passID);

        btnLogIn.setOnClickListener(this);
        txtCreatAccount.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLogInID:
            {
                Intent in=new Intent(this,MainActivity.class);
                startActivity(in);
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
    public void userLogin() {
        String userName = edtxtUserName.getText().toString().trim();
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
