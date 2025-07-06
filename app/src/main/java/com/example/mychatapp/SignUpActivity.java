package com.example.mychatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mychatapp.Models.Users;
import com.example.mychatapp.databinding.ActivitySignUpBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private static final int RC_SIGN_IN = 65;
    private ActivitySignUpBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setupClickListeners();
    }

    private void initializeComponents() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("Please wait while we create your account...");
        progressDialog.setCancelable(false);

        // Initialize Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        binding.btnSignUp.setOnClickListener(v -> handleEmailSignUp());
        binding.btnGoogle.setOnClickListener(v -> handleGoogleSignIn());
        binding.ExistingAccount.setOnClickListener(v -> navigateToSignIn());

        binding.btnfb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignUpActivity.this, "Facebook login is not available yet.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.signUpPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignUpActivity.this, "Phone number login is not available yet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleEmailSignUp() {
        String username = binding.etUserName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (!validateInputs(username, email, password)) {
            return;
        }

        progressDialog.show();

        // 🔍 Check if email is already registered via Google
        auth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(result -> {
                    List<String> signInMethods = result.getSignInMethods();
                    if (signInMethods != null && signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD)) {
                        progressDialog.dismiss();
                        showErrorMessage("This email is already registered using Google. Please sign in with Google.");
                    } else {
                        // ✅ Safe to register using email & password
                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this, task -> {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        handleSuccessfulEmailSignUp(username, email, password, task);
                                    } else {
                                        handleSignUpFailure(task.getException());
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showErrorMessage("Failed to validate email: " + e.getMessage());
                });
    }


    private boolean validateInputs(String username, String email, String password) {
        // Reset previous errors
        binding.etUserName.setError(null);
        binding.etEmail.setError(null);
        binding.etPassword.setError(null);

        if (TextUtils.isEmpty(username)) {
            binding.etUserName.setError("Username is required");
            binding.etUserName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email address");
            binding.etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters");
            binding.etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void handleSuccessfulEmailSignUp(String username, String email, String password, Task<AuthResult> task) {
        try {
            FirebaseUser firebaseUser = task.getResult().getUser();
            if (firebaseUser != null) {
                firebaseUser.sendEmailVerification()
                        .addOnCompleteListener(emailTask -> {
                            if (emailTask.isSuccessful()) {
                                Toast.makeText(this, "Verification email sent to " + firebaseUser.getEmail(), Toast.LENGTH_LONG).show();

                                // Optional: log user out after registration
                                auth.signOut();

                                // Go to login screen
                                Intent intent = new Intent(this, SignInActivity.class);
                                intent.putExtra("verify_notice", true);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e(TAG, "Failed to send verification email", emailTask.getException());
                                showErrorMessage("Failed to send verification email. Try again later.");
                            }
                        });

                // You can still create user record in DB
                Users user = new Users(username, email, "", "Hey there! I am using MyChatApp.");
                String userId = firebaseUser.getUid();

                database.getReference()
                        .child("Users")
                        .child(userId)
                        .setValue(user)
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to save user data", e);
                            showErrorMessage("Failed to save user data");
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in handleSuccessfulEmailSignUp", e);
            showErrorMessage("An error occurred during sign up");
        }
    }

    private void handleGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed", e);
                showErrorMessage("Google sign in failed");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        String email = account.getEmail();
        if (email == null) {
            showErrorMessage("Failed to retrieve email from Google account.");
            return;
        }

        // Step 1: Check if email already exists with another provider
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isEmailRegisteredWithPassword = task.getResult()
                                .getSignInMethods()
                                .contains("password");

                        if (isEmailRegisteredWithPassword) {
                            // 🚫 Show warning and stop Google login
                            showErrorMessage("This email is already registered using a password. Please log in using email and password.");
                            return;
                        }

                        // ✅ Safe to proceed with Google sign-in
                        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                        auth.signInWithCredential(credential)
                                .addOnCompleteListener(this, authTask -> {
                                    if (authTask.isSuccessful()) {
                                        handleSuccessfulGoogleSignIn();
                                    } else {
                                        Log.e(TAG, "Firebase auth with Google failed", authTask.getException());
                                        showErrorMessage("Authentication failed");
                                    }
                                });

                    } else {
                        showErrorMessage("Failed to verify sign-in method: " + task.getException().getMessage());
                        Log.e(TAG, "fetchSignInMethodsForEmail failed", task.getException());
                    }
                });
    }

    private void handleSuccessfulGoogleSignIn() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            Users user = new Users();
            user.setUserId(firebaseUser.getUid());
            user.setUserName(firebaseUser.getDisplayName());
            user.setAbout("Hey there! I am using MyChatApp.");

            // Check if profile picture exists
            if (firebaseUser.getPhotoUrl() != null) {
                user.setProfilepic(firebaseUser.getPhotoUrl().toString());
            }

            String userId = firebaseUser.getUid();

            database.getReference()
                    .child("Users")
                    .child(userId)
                    .setValue(user)
                    .addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            setupFCMToken(userId);
                            showSuccessMessage("Google sign in successful!");
                            navigateToHome();
                        } else {
                            Log.e(TAG, "Failed to save Google user data", dbTask.getException());
                            showErrorMessage("Failed to save user data");
                        }
                    });
        }
    }

    private void setupFCMToken(String userId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        database.getReference()
                                .child("Users")
                                .child(userId)
                                .child("FCMToken")
                                .setValue(token)
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Failed to save FCM token", e));
                    } else {
                        Log.e(TAG, "Failed to get FCM token", task.getException());
                    }
                });
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleSignUpFailure(Exception exception) {
        Log.e(TAG, "Sign up failed", exception);
        if (exception != null) {
            showErrorMessage("Sign up failed: " + exception.getMessage());
        } else {
            showErrorMessage("Sign up failed. Please try again.");
        }
    }

    private void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        binding = null;
    }
}