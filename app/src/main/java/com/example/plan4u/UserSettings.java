package com.example.plan4u;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserSettings extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private String onlineUserID = mUser.getUid();
    final  String authUserEmailAddress = mUser.getEmail();

    private TextView userEmailId;
    private TextView userID;
    private TextView backButton;

    private Button settingsChangePassword;
    private Button settingsDeleteAccount;

    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        loader = new ProgressDialog(this);

        userEmailId =  (TextView)findViewById(R.id.settingsUserEmail);
        userEmailId.setText(authUserEmailAddress);

        userID = (TextView) findViewById(R.id.settingsUserID);
        userID.setText(onlineUserID);

        backButton = (TextView) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserSettings.this,Home.class);
                startActivity(intent);
                finish();
            }
        });

        settingsChangePassword = (Button) findViewById(R.id.settingsChangePassword);
        settingsChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loader.setMessage("Sending Link to Change Password to your email");
                loader.show();
                mAuth.sendPasswordResetEmail(authUserEmailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            loader.dismiss();
                            Toast.makeText(UserSettings.this,"Link to change password has been sent to your registered email!",Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            Intent intent1 = new Intent(UserSettings.this,LoginActivity.class);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent1);
                            finish();
                        }
                        else {
                            String error = task.getException().toString();
                            Toast.makeText(UserSettings.this,"An unexpected error has occured ! Try again Later. " + error,Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        settingsDeleteAccount = (Button) findViewById(R.id.settingsDeleteAccount);

        settingsDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loader.setMessage("Deleting account");
                loader.show();
                mUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(UserSettings.this,"Account Deleted",Toast.LENGTH_LONG).show();
                            loader.dismiss();
                            Intent intent = new Intent(UserSettings.this,LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            String error = task.getException().toString();
                            Toast.makeText(UserSettings.this,"Something went wrong! Please Try again. " + error,Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}