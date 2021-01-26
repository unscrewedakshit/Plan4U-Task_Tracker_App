package com.example.plan4u;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity {


    EditText registrationEmail,registrationPassword,registrationConfirmPassword,registrationUsername;
    Button registrationButton;
    TextView registrationQuestion;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        loader = new ProgressDialog(this);

        registrationUsername = (EditText) findViewById(R.id.RegistrationName);
        registrationEmail = (EditText) findViewById(R.id.RegistrationEmail);
        registrationPassword = (EditText) findViewById(R.id.RegistrationPassword);
        registrationConfirmPassword = (EditText) findViewById(R.id.RegistrationConfirmPassword);
        registrationButton = (Button) findViewById(R.id.RegistrationButton);
        registrationQuestion = (TextView) findViewById(R.id.RegistrationPageQuestion);

        registrationQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrationActivity.this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = registrationEmail.getText().toString().trim();
                String password = registrationPassword.getText().toString().trim();
                String confirmPassword = registrationConfirmPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)) {
                    registrationEmail.setError("Please enter your email");
                    return;
                }
                else if(TextUtils.isEmpty(password)) {
                    registrationPassword.setError("Password cannot be empty");
                    return;
                }
                else if(!registrationPassword.getText().toString().equals(registrationConfirmPassword.getText().toString())) {
                    registrationConfirmPassword.setError("Password does not match");
                    return;
                }
                else {
                    loader.setMessage("Authenticating with Database");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {

                                FirebaseUser user = mAuth.getCurrentUser();
                                assert user != null;
                                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loader.dismiss();
                                        Toast.makeText(RegistrationActivity.this,"Account Registered! Check your Inbox for proceed",Toast.LENGTH_LONG).show();
                                        registrationEmail.setText("");
                                        registrationPassword.setText("");
                                        registrationConfirmPassword.setText("");
                                        registrationUsername.setText("");
                                        mAuth.signOut();
                                    }
                                });


                                //Intent intent = new Intent(RegistrationActivity.this,HomeActivity.class);
                                //startActivity(intent);
                                //finish();
                            }
                            else {
                                String error = task.getException().toString();
                                Toast.makeText(RegistrationActivity.this,"Registration Failed" + error,Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });
                }

            }
        });
    }

}