package com.example.plan4u;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail,loginPassword;
    Button loginButton;
    TextView loginQuestion,forgotPassword;

    private FirebaseAuth mAuth;
    private  FirebaseUser user;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        loader = new ProgressDialog(this);

        loginEmail = (EditText) findViewById(R.id.loginEmail);
        loginPassword = (EditText) findViewById(R.id.loginPassword);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginQuestion = (TextView) findViewById(R.id.loginPageQuestion);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);

        if(user != null) {
            Intent i = new Intent(LoginActivity.this, Home.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }



            loginQuestion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                    startActivity(intent);
                }
            });

            forgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                    startActivity(intent);
                }
            });

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = loginEmail.getText().toString().trim();
                    String password = loginPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(email)) {
                        loginEmail.setError("Please enter your email");
                        return;
                    } else if (TextUtils.isEmpty(password)) {
                        loginPassword.setError("Please enter your password");
                        return;
                    } else {
                        loader.setMessage("Authenticating user");
                        loader.setCanceledOnTouchOutside(false);
                        loader.show();

                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if (mAuth.getCurrentUser().isEmailVerified()) {
                                        Intent intent = new Intent(LoginActivity.this, Home.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Please verify your Email to continue", Toast.LENGTH_SHORT).show();
                                    }
                                    loader.dismiss();
                                } else {
                                    String error = task.getException().toString();
                                    Toast.makeText(LoginActivity.this, "Login Failed" + error, Toast.LENGTH_SHORT).show();
                                    loader.dismiss();
                                }

                            }
                        });
                    }

                }
            });
    }
}
