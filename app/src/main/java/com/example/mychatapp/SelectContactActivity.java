package com.example.mychatapp;

import android.os.Bundle;
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
    ActivitySelectContactBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    SelectContctAdapter adapter;
    ArrayList<Users> userList;
    LinearLayoutManager layoutManager;
    private ShimmerFrameLayout shimmerFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySelectContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialize shimmer
        shimmerFrameLayout = binding.shimmerViewContainer;

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userList = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this);
        adapter = new SelectContctAdapter(userList);
        binding.selectChatRV.setAdapter(adapter);
        binding.selectChatRV.setLayoutManager(layoutManager);

        // Start shimmer effect
        startShimmer();

        binding.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.refreshButton.setVisibility(View.GONE);
                binding.refreshProgress.setVisibility(View.VISIBLE);

                // Show shimmer during refresh
                startShimmer();
                loadUsers();
            }
        });

        // Load users from Firebase
        loadUsers();
    }

    private void startShimmer() {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        binding.selectChatRV.setVisibility(View.GONE);
    }

    private void stopShimmer() {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        binding.selectChatRV.setVisibility(View.VISIBLE);
    }

    private void loadUsers() {
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Users users = dataSnapshot.getValue(Users.class);
                    if (users != null) {
                        users.setUserId(dataSnapshot.getKey());

                        if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())){
                            userList.add(users);
                        }
                    }
                }
                adapter.notifyDataSetChanged();

                // Stop shimmer and show RecyclerView
                stopShimmer();
                binding.refreshProgress.setVisibility(View.GONE);
                binding.refreshButton.setVisibility(View.VISIBLE);

                // Update subtitle with actual count
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(userList.size() + " contacts");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Stop shimmer even if there's an error
                stopShimmer();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume shimmer if it was running
        if (shimmerFrameLayout.getVisibility() == View.VISIBLE) {
            shimmerFrameLayout.startShimmer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause shimmer to save resources
        shimmerFrameLayout.stopShimmer();
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