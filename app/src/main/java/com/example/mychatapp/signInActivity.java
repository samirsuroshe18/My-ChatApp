package com.example.mychatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.mychatapp.Models.Users;
import com.example.mychatapp.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class signInActivity extends Activity {

    static final int RC_SIGN_IN = 65;
    ActivitySignInBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onStart() {
        super.onStart();

        if (auth.getCurrentUser() != null){
            Intent intent = new Intent(signInActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(signInActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Login to your account");


        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (binding.etEmail.getText().toString().isEmpty()) {
                    binding.etEmail.setError("Enter Email");
                } else if (binding.etPassword.getText().toString().isEmpty()) {
                    binding.etPassword.setError("Enter Password");
                }
                else {
                    progressDialog.show();
                    auth.signInWithEmailAndPassword(binding.etEmail.getText().toString(), binding.etPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(signInActivity.this, HomeActivity.class);
                                startActivity(intent);

                                finish();
                            } else {
                                Toast.makeText(signInActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });

        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();

                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(signInActivity.this, gso);

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                int RC_SIGN_IN = 65;
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        binding.ExistingAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signInActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    auth.signInWithCredential(credential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        FirebaseUser user = auth.getCurrentUser();
                                        // User is signed in, you can access user information here

                                        Users users = new Users();
                                        users.setUserId(user.getUid());
                                        users.setUserName(user.getDisplayName());
                                        users.setProfilepic(user.getPhotoUrl().toString());

                                        database.getReference().child("Users").child(user.getUid()).setValue(users);

                                        Intent intent = new Intent(signInActivity.this, HomeActivity.class);
                                        startActivity(intent);

                                        finish();
                                    } else {
                                        // Handle sign-in failure
                                        Toast.makeText(signInActivity.this, "User Log in un-successful" +task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            } catch (ApiException e) {
                // Handle sign-in failure
                e.printStackTrace();
            }
        }
    }

}