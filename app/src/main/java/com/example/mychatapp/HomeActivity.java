package com.example.mychatapp;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mychatapp.Fragments.HomeFragment;
import com.example.mychatapp.Fragments.UserProfileFragment;
import com.example.mychatapp.databinding.ActivityHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private static final String STATUS_ONLINE = "online";
    private static final String BUNDLE_KEY_ACTIVE_FRAGMENT = "active_fragment";

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Set offline timestamp
        setUserStatus(String.valueOf(System.currentTimeMillis()));

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

    private void setUserStatus(@NonNull String status) {
        if (userStatusRef != null) {
            userStatusRef.setValue(status)
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update user status", e));
        }
    }
}