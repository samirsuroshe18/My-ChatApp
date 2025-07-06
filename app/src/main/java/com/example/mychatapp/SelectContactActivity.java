package com.example.mychatapp;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mychatapp.Adapters.SelectContctAdapter;
import com.example.mychatapp.Models.Users;
import com.example.mychatapp.databinding.ActivitySelectContactBinding;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SelectContactActivity extends AppCompatActivity {
    private ActivitySelectContactBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private SelectContctAdapter adapter;
    private ArrayList<Users> userList;
    private ShimmerFrameLayout shimmerFrameLayout;
    private ValueEventListener usersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupRefreshButton();
        loadUsers();
    }

    private void initializeViews() {
        EdgeToEdge.enable(this);
        binding = ActivitySelectContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        shimmerFrameLayout = binding.shimmerViewContainer;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Select Contact");
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new SelectContctAdapter(userList);
        binding.selectChatRV.setAdapter(adapter);
        binding.selectChatRV.setLayoutManager(layoutManager);
    }

    private void setupRefreshButton() {
        binding.refreshButton.setOnClickListener(v -> {
            binding.refreshButton.setVisibility(View.GONE);
            binding.refreshProgress.setVisibility(View.VISIBLE);
            startShimmer();
            loadUsers();
        });
    }

    private void startShimmer() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
            binding.selectChatRV.setVisibility(View.GONE);
        }
    }

    private void stopShimmer() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            binding.selectChatRV.setVisibility(View.VISIBLE);
        }
    }

    private void loadUsers() {
        startShimmer();

        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = new Users();
                    user.setProfilepic(dataSnapshot.child("profilepic").getValue(String.class));
                    user.setUserName(dataSnapshot.child("userName").getValue(String.class));
                    user.setAbout(dataSnapshot.child("about").getValue(String.class));
                    user.setUserId(dataSnapshot.getKey());

                    if (!user.getUserId().equals(currentUserId)) {
                        userList.add(user);
                    }
                }

                adapter.notifyDataSetChanged();
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleError(error);
            }
        };

        database.getReference().child("Users").addListenerForSingleValueEvent(usersListener);
    }

    private void updateUI() {
        stopShimmer();
        binding.refreshProgress.setVisibility(View.GONE);
        binding.refreshButton.setVisibility(View.VISIBLE);
        updateSubtitle();
    }

    private void updateSubtitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String subtitle = userList.size() == 1 ? "1 contact" : userList.size() + " contacts";
            actionBar.setSubtitle(subtitle);
        }
    }

    private void handleError(DatabaseError error) {
        stopShimmer();
        binding.refreshProgress.setVisibility(View.GONE);
        binding.refreshButton.setVisibility(View.VISIBLE);

        // Log error or show user-friendly message
         Log.e("SelectContactActivity", "Database error: " + error.getMessage());
    }

    private String getSnapshotType(DataSnapshot snapshot) {
        Object value = snapshot.child("status").getValue();

        if (value == null) return "null";
        if (value instanceof String) return "String";
        if (value instanceof Long) return "Long";
        if (value instanceof Integer) return "Integer";
        if (value instanceof Boolean) return "Boolean";
        if (value instanceof Double) return "Double";

        return value.getClass().getSimpleName(); // fallback
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shimmerFrameLayout != null && shimmerFrameLayout.getVisibility() == View.VISIBLE) {
            shimmerFrameLayout.startShimmer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up listeners to prevent memory leaks
        if (usersListener != null && database != null) {
            database.getReference().child("Users").removeEventListener(usersListener);
        }
        // Clean up binding
        binding = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}