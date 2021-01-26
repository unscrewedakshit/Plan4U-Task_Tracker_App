package com.example.plan4u;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPassword extends AppCompatActivity {

    private EditText forgotPasswordEmail;
    private Button forgotPasswordButton;
    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forogot_password);

        forgotPasswordEmail = (EditText) findViewById(R.id.forgotPasswordEmail);
        forgotPasswordButton = (Button) findViewById(R.id.forgotPasswordButton);
        mAuth = FirebaseAuth.getInstance();
        loader = new ProgressDialog(this);


        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = forgotPasswordEmail.getText().toString().trim();
                if(TextUtils.isEmpty(email)) {
                    forgotPasswordEmail.setError("Email cannot be empty");
                    return;
                }
                loader.setMessage("Sending password reset link");
                loader.show();
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(ForgotPassword.this,"Password reset link sent",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ForgotPassword.this,LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            String error = task.getException().toString();
                            Toast.makeText(ForgotPassword.this,"Something went please wrong try again! " + error,Toast.LENGTH_LONG).show();
                            forgotPasswordEmail.setText("");
                        }
                        loader.dismiss();
                    }
                });
            }
        });
    }
}