package com.example.mychatapp;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.mychatapp.Fragments.HomeFragment;
import com.example.mychatapp.Fragments.UserProfileFragment;
import com.example.mychatapp.databinding.ActivityHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {
    ActivityHomeBinding binding;
    FirebaseAuth auth;
    private HomeFragment homeFragment;
    private UserProfileFragment userProfileFragment;
    private Fragment activeFragment;
    String senderId;

    @Override
    protected void onResume() {
        super.onResume();
        setUserStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setUserStatus(String status) {
        if (senderId != null) {
            FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(senderId)
                    .child("status")
                    .setValue(status);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize fragments once
        auth = FirebaseAuth.getInstance();
        senderId = auth.getUid();
        homeFragment = new HomeFragment();
        userProfileFragment = new UserProfileFragment();

        binding.bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.navigation_home) {
                    selectedFragment = homeFragment;
                } else if (id == R.id.navigation_dashboard) {
                    selectedFragment = userProfileFragment;
                }

                if (selectedFragment != null && selectedFragment != activeFragment) {
                    showFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(senderId)
                .child("status");

        ref.onDisconnect().setValue(String.valueOf(System.currentTimeMillis()));

        // Load default fragment
        if (savedInstanceState == null) {
            showFragment(homeFragment);
            binding.bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    private void showFragment(Fragment fragment) {
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
    protected void onDestroy() {
        super.onDestroy();
        long timestamp = System.currentTimeMillis();
        setUserStatus(String.valueOf(timestamp));
    }
}