package com.example.mychatapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mychatapp.Models.Users;
import com.example.mychatapp.databinding.ActivitySignInBinding;
import com.example.mychatapp.dialogs.ProgressDialogFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Activity responsible for user authentication via email/password and Google Sign-In.
 * Implements modern Android best practices including ViewBinding, Activity Result API,
 * proper null safety, and comprehensive error handling.
 */
public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    private static final String PROGRESS_DIALOG_TAG = "ProgressDialog";

    // View binding for type-safe view access
    private ActivitySignInBinding binding;

    // Firebase authentication and database instances
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    // Progress dialog for loading states
    private ProgressDialogFragment progressDialog;

    // Activity result launcher for Google Sign-In
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize view binding
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFirebaseComponents();
        initializeProgressDialog();
        setupGoogleSignInLauncher();
        setupClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserAuthentication();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up view binding to prevent memory leaks
        binding = null;
    }

    /**
     * Initialize Firebase authentication and database components
     */
    private void initializeFirebaseComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Initialize progress dialog with loading message
     */
    private void initializeProgressDialog() {
        progressDialog = ProgressDialogFragment.newInstance(
                "My Custom Title",
                "This is the subtitle or status message"
        );
    }

    /**
     * Check if user is already authenticated and redirect if necessary
     */
    private void checkUserAuthentication() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already authenticated, redirecting to home");
            navigateToHome();
        }
    }

    /**
     * Setup Activity Result Launcher for Google Sign-In with proper error handling
     */
    private void setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
//                    dismissProgressDialog();

                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleGoogleSignInResult(result.getData());
                    } else {
                        Log.w(TAG, "Google Sign-In cancelled or failed");
                        showToast("Google Sign-In was cancelled");
                    }
                }
        );
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Email/Password sign in
        binding.btnSignUp.setOnClickListener(v -> handleEmailPasswordSignIn());

        // Google sign in
        binding.btnGoogle.setOnClickListener(v -> initiateGoogleSignIn());

        // Navigate to registration/main activity
        binding.ExistingAccount.setOnClickListener(v -> navigateToRegistration());
    }

    /**
     * Handle email and password sign-in with comprehensive validation
     */
    private void handleEmailPasswordSignIn() {
        String email = getTextFromEditText(binding.etEmail);
        String password = getTextFromEditText(binding.etPassword);

        // Validate input fields
        if (!validateEmailAndPassword(email, password)) {
            return;
        }

//        showProgressDialog();

        assert password != null;
        assert email != null;
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
//                    dismissProgressDialog();

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email/Password sign-in successful");
                        navigateToHome();
                    } else {
                        handleAuthenticationError("Email/Password sign-in failed", task.getException());
                    }
                });
    }

    /**
     * Initiate Google Sign-In process
     */
    private void initiateGoogleSignIn() {
//        showProgressDialog();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * Handle Google Sign-In result with proper error handling
     */
    private void handleGoogleSignInResult(@NonNull Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                authenticateWithFirebase(account);
            } else {
                Log.e(TAG, "Google Sign-In account or ID token is null");
                showToast("Google Sign-In failed: Invalid account data");
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google Sign-In failed", e);
            showToast("Google Sign-In failed: " + e.getMessage());
        }
    }

    /**
     * Authenticate with Firebase using Google credentials
     */
    private void authenticateWithFirebase(@NonNull GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase authentication successful");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToDatabase(user);
                        }
                    } else {
                        handleAuthenticationError("Firebase authentication failed", task.getException());
                    }
                });
    }

    /**
     * Save user information to Firebase Database with null safety
     */
    private void saveUserToDatabase(@NonNull FirebaseUser firebaseUser) {
        Users user = createUserFromFirebaseUser(firebaseUser);

        if (user != null) {
            databaseReference.child("Users").child(firebaseUser.getUid())
                    .setValue(user)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User data saved successfully");
                            navigateToHome();
                        } else {
                            Log.e(TAG, "Failed to save user data", task.getException());
                            // Still navigate to home as authentication was successful
                            navigateToHome();
                        }
                    });
        } else {
            Log.e(TAG, "Failed to create user object from FirebaseUser");
            // Still navigate to home as authentication was successful
            navigateToHome();
        }
    }

    /**
     * Create User object from FirebaseUser with null safety
     */
    @Nullable
    private Users createUserFromFirebaseUser(@NonNull FirebaseUser firebaseUser) {
        try {
            Users user = new Users();
            user.setUserId(firebaseUser.getUid());
            user.setUserName(firebaseUser.getDisplayName());

            // Handle potential null photo URL
            if (firebaseUser.getPhotoUrl() != null) {
                user.setProfilepic(firebaseUser.getPhotoUrl().toString());
            } else {
                user.setProfilepic(""); // Default empty string or use a default profile pic URL
            }

            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error creating user object", e);
            return null;
        }
    }

    /**
     * Validate email and password inputs
     */
    private boolean validateEmailAndPassword(@Nullable String email, @Nullable String password) {
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            return false;
        }

        // Clear any previous errors
        binding.etEmail.setError(null);
        binding.etPassword.setError(null);

        return true;
    }

    /**
     * Safely extract text from EditText with null checking
     */
    @Nullable
    private String getTextFromEditText(@NonNull android.widget.EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : null;
    }

    /**
     * Handle authentication errors with logging and user feedback
     */
    private void handleAuthenticationError(@NonNull String logMessage, @Nullable Exception exception) {
        if (exception != null) {
            Log.e(TAG, logMessage, exception);
            showToast(logMessage + ": " + exception.getMessage());
        } else {
            Log.e(TAG, logMessage);
            showToast(logMessage);
        }
    }

    /**
     * Navigate to home activity and finish current activity
     */
    private void navigateToHome() {
        String userId = FirebaseAuth.getInstance().getUid();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful()){
                    databaseReference.child("Users").child(userId).child("FCMToken").setValue(task.getResult());
                }
            }
        });
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate to registration/main activity
     */
    private void navigateToRegistration() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Show progress dialog safely
     */
    private void showProgressDialog() {
        if (!isFinishing() && !isDestroyed()) {
            progressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
        }
    }

    /**
     * Dismiss progress dialog safely
     */
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isAdded()) {
            progressDialog.dismiss();
        }
    }

    /**
     * Show toast message safely
     */
    private void showToast(@NonNull String message) {
        if (!isFinishing() && !isDestroyed()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}