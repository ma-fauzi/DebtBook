package com.example.pengendaliapi.debtbook;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText etUser, etPassword;
    Button bLoginButton;
    TextView tvForgotPassword;
    FirebaseAuth fbAuth;
    FirebaseDatabase fbDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUser = (EditText)findViewById(R.id.user);
        etPassword = (EditText)findViewById(R.id.password);
        bLoginButton = (Button)findViewById(R.id.loginButton);
        tvForgotPassword = (TextView)findViewById(R.id.forgotPassword);
        fbAuth = FirebaseAuth.getInstance();
        fbDatabase = FirebaseDatabase.getInstance();

        bLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUser.getText().toString();
                String password = etPassword.getText().toString();

                if (!user.isEmpty() && !password.isEmpty()) {
                    login();
                } else {
                    Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvForgotPassword.setPaintFlags(tvForgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etUser.getText().toString().isEmpty()) {
                    fbAuth.getInstance().sendPasswordResetEmail(etUser.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(LoginActivity.this, "E-mail sent", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this, "Please insert your e-mail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void login() {
        final String email = etUser.getText().toString();
        final String username = etUser.getText().toString();
        final String password = etPassword.getText().toString();

        fbAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    sendUserToDashboardActivity();
                    Toast.makeText(LoginActivity.this, "Login success!", Toast.LENGTH_SHORT).show();
                } else {
                    fbDatabase.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot uidSnapshot : dataSnapshot.getChildren()) {
                                String un = uidSnapshot.child("username").getValue().toString();

                                if (un.equals(username)) {
                                    String email = uidSnapshot.child("email").getValue().toString();

                                    fbAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                sendUserToDashboardActivity();
                                                Toast.makeText(LoginActivity.this, "Login success!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Please recheck your email or password", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    private void sendUserToDashboardActivity(){
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
