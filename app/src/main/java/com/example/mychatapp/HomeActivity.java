package com.example.mychatapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mychatapp.Fragments.HomeFragment;
import com.example.mychatapp.Fragments.UserProfileFragment;
import com.example.mychatapp.databinding.ActivityHomeBinding;
import com.example.mychatapp.utils.BatteryOptimizationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private static final String STATUS_ONLINE = "online";
    private static final String BUNDLE_KEY_ACTIVE_FRAGMENT = "active_fragment";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int SETTINGS_REQUEST_CODE = 1002;
    // SharedPreferences keys
    private static final String PREF_BATTERY_OPTIMIZATION_ASKED = "battery_optimization_asked";
    private static final String PREF_BATTERY_OPTIMIZATION_DISMISSED_COUNT = "battery_optimization_dismissed_count";
    private static final int MAX_BATTERY_OPTIMIZATION_PROMPTS = 3;

    // View binding
    private ActivityHomeBinding binding;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference userStatusRef;
    private String senderId;

    // Fragments
    private HomeFragment homeFragment;
    private UserProfileFragment userProfileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize view binding
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        setupWindowInsets();

        // Initialize Firebase and user data
        if (!initializeFirebase()) {
            finish();
            return;
        }

        // Initialize fragments
        initializeFragments();

        // Setup bottom navigation
        setupBottomNavigation();

        // Setup user status tracking
        setupUserStatusTracking();

        // Load initial fragment
        loadInitialFragment(savedInstanceState);

        checkAndRequestNotificationPermission();

        checkBatteryOptimizationWithDelay();
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Check if we should show rationale
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {
                    showPermissionRationale();
                } else {
                    // Request permission directly
                    requestNotificationPermission();
                }
            } else {
                Log.d(TAG, "Notification permission already granted");
            }
        } else {
            // For Android 12 and below, notifications are enabled by default
            Log.d(TAG, "Notification permission not required for this Android version");
        }
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Notification Permission Required")
                .setMessage("This app needs notification permission to show you new chat messages. " +
                        "Without this permission, you won't receive message notifications.")
                .setPositiveButton("Grant Permission", (dialog, which) -> requestNotificationPermission())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "You can enable notifications later in Settings",
                            Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Notification permission denied");
                handlePermissionDenied();
            }
        }
    }

    private void handlePermissionDenied() {
        // Check if user selected "Don't ask again"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {

            // User selected "Don't ask again", show dialog to go to settings
            showSettingsDialog();
        } else {
            // User just denied, show info
            Toast.makeText(this, "Notification permission denied. You can enable it later in Settings.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable Notifications")
                .setMessage("To receive chat notifications, please enable notification permission in Settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, SETTINGS_REQUEST_CODE);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "You can enable notifications later in Settings",
                            Toast.LENGTH_LONG).show();
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST_CODE) {
            // Check if permission was granted in settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Notification permission enabled!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkBatteryOptimizationWithDelay() {
        // Add a slight delay to not overwhelm user with multiple dialogs
        // and to ensure notification permission dialog is handled first
        new android.os.Handler().postDelayed(() -> {
            checkBatteryOptimization();
        }, 2000); // 2 second delay
    }

    private void checkBatteryOptimization() {
        // Check if we should show battery optimization dialog
        if (shouldShowBatteryOptimizationDialog()) {
            if (BatteryOptimizationUtils.isBatteryOptimizationEnabled(this)) {
                showBatteryOptimizationDialog();
            } else {
                Log.d(TAG, "Battery optimization already disabled");
            }
        }
    }

    private boolean shouldShowBatteryOptimizationDialog() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int dismissedCount = prefs.getInt(PREF_BATTERY_OPTIMIZATION_DISMISSED_COUNT, 0);

        // Don't show if user has dismissed it too many times
        if (dismissedCount >= MAX_BATTERY_OPTIMIZATION_PROMPTS) {
            Log.d(TAG, "Battery optimization dialog dismissed too many times, not showing");
            return false;
        }

        // Show only if it's needed
        return BatteryOptimizationUtils.isBatteryOptimizationEnabled(this);
    }

    private void showBatteryOptimizationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Improve Notification Reliability")
                .setMessage("To ensure you receive chat notifications reliably, especially after device restart, " +
                        "please disable battery optimization for this app.\n\n" +
                        "This won't significantly impact your battery life.")
                .setPositiveButton("Disable Optimization", (dialog, which) -> {
                    BatteryOptimizationUtils.requestBatteryOptimizationExemption(this);
                    markBatteryOptimizationAsked();
                })
                .setNegativeButton("Skip", (dialog, which) -> {
                    incrementBatteryOptimizationDismissCount();
                    Toast.makeText(this, "You can change this later in Settings", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Don't Ask Again", (dialog, which) -> {
                    // Set dismissed count to max so it won't be shown again
                    SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    prefs.edit().putInt(PREF_BATTERY_OPTIMIZATION_DISMISSED_COUNT, MAX_BATTERY_OPTIMIZATION_PROMPTS).apply();
                    Toast.makeText(this, "You can enable this later in Settings", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }

    private void markBatteryOptimizationAsked() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_BATTERY_OPTIMIZATION_ASKED, true).apply();
    }

    private void incrementBatteryOptimizationDismissCount() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int currentCount = prefs.getInt(PREF_BATTERY_OPTIMIZATION_DISMISSED_COUNT, 0);
        prefs.edit().putInt(PREF_BATTERY_OPTIMIZATION_DISMISSED_COUNT, currentCount + 1).apply();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "User not authenticated");
            return false;
        }

        senderId = currentUser.getUid();
        userStatusRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(senderId)
                .child("status");

        return true;
    }

    private void initializeFragments() {
        homeFragment = new HomeFragment();
        userProfileFragment = new UserProfileFragment();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = getFragmentForMenuItem(item.getItemId());

        if (selectedFragment != null && selectedFragment != activeFragment) {
            showFragment(selectedFragment);
            return true;
        }
        return false;
    }

    @Nullable
    private Fragment getFragmentForMenuItem(int itemId) {
        if (itemId == R.id.navigation_home) {
            return homeFragment;
        } else if (itemId == R.id.navigation_dashboard) {
            return userProfileFragment;
        }
        return null;
    }

    private void setupUserStatusTracking() {
        if (userStatusRef != null) {
            // Set offline status when connection is lost
            userStatusRef.onDisconnect().setValue(System.currentTimeMillis());
        }
    }

    private void loadInitialFragment(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            showFragment(homeFragment);
            binding.bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        } else {
            // Handle fragment restoration if needed
            restoreFragmentState(savedInstanceState);
        }
    }

    private void restoreFragmentState(@NonNull Bundle savedInstanceState) {
        // You can implement fragment state restoration here if needed
        // For now, just load the default fragment
        showFragment(homeFragment);
        binding.bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void showFragment(@NonNull Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Hide current active fragment
        if (activeFragment != null) {
            transaction.hide(activeFragment);
        }

        // Show selected fragment
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.fragment_container, fragment);
        }

        transaction.commit();
        activeFragment = fragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserStatus(STATUS_ONLINE);
        // Check if battery optimization was disabled when returning from settings
        if (!BatteryOptimizationUtils.isBatteryOptimizationEnabled(this)) {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            boolean wasAsked = prefs.getBoolean(PREF_BATTERY_OPTIMIZATION_ASKED, false);

            if (wasAsked) {
                Toast.makeText(this, "Battery optimization disabled! Notifications will be more reliable.",
                        Toast.LENGTH_SHORT).show();
                // Reset the flag
                prefs.edit().putBoolean(PREF_BATTERY_OPTIMIZATION_ASKED, false).apply();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Set offline timestamp
        setUserStatus(System.currentTimeMillis());

        // Clean up binding
        binding = null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save active fragment state if needed
        if (activeFragment != null) {
            outState.putString(BUNDLE_KEY_ACTIVE_FRAGMENT, activeFragment.getClass().getSimpleName());
        }
    }

    private void setUserStatus(@NonNull Object status) {
        if (userStatusRef != null) {
            userStatusRef.setValue(status)
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update user status", e));
        }
    }
}