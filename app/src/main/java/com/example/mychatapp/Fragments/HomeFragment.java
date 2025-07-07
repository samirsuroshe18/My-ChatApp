package com.example.mychatapp.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mychatapp.Adapters.UsersAdapter;
import com.example.mychatapp.Models.ChatlistModel;
import com.example.mychatapp.SelectContactActivity;
import com.example.mychatapp.databinding.FragmentChatListBinding;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FragmentChatListBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference chatListRef;
    private ArrayList<ChatlistModel> chatList;
    private ArrayList<ChatlistModel> filteredChatList;

    private UsersAdapter adapter;
    private ValueEventListener chatListListener;
    private ShimmerFrameLayout shimmerFrameLayout;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            chatListRef = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("ChatList")
                    .child(auth.getUid());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isUserAuthenticated()) {
            loadChatList();
        } else {
            handleUnauthenticatedUser();
        }
    }

    private void initializeViews() {
        shimmerFrameLayout = binding.shimmerChatContainer;
        chatList = new ArrayList<>();
        filteredChatList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        if (getContext() != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            adapter = new UsersAdapter(filteredChatList);

            binding.chatListItem.setLayoutManager(layoutManager);
            binding.chatListItem.setAdapter(adapter);
            binding.chatListItem.setHasFixedSize(true); // Performance optimization
        }
    }

    private void setupClickListeners() {
        binding.selectContact.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), SelectContactActivity.class);
                startActivity(intent);
            }
        });

        binding.allChatsChip.setOnClickListener(v -> {
            binding.unreadChip.setChecked(false);
            binding.allChatsChip.setChecked(true);
            applyFilter();
        });

        binding.unreadChip.setOnClickListener(v -> {
            binding.allChatsChip.setChecked(false);
            binding.unreadChip.setChecked(true);
            applyFilter();
        });
    }

    private boolean isUserAuthenticated() {
        return auth != null && auth.getCurrentUser() != null && chatListRef != null;
    }

    private void handleUnauthenticatedUser() {
        stopShimmer();
        showEmptyState();
        Log.w(TAG, "User not authenticated");
    }

    private void loadChatList() {
        showShimmer();
        attachChatListListener();
    }

    private void attachChatListListener() {
        // Remove existing listener to prevent memory leaks
        removeChatListListener();

        chatListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                processChatListData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
            }
        };

        chatListRef.addValueEventListener(chatListListener);
    }

    private void processChatListData(@NonNull DataSnapshot snapshot) {
        chatList.clear();

        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            ChatlistModel chatModel = parseChatListItem(dataSnapshot);
            if (chatModel != null && isValidChatItem(chatModel)) {
                chatList.add(chatModel);
            }
        }

        updateUI();
    }

    @Nullable
    private ChatlistModel parseChatListItem(@NonNull DataSnapshot dataSnapshot) {
        try {
            ChatlistModel chatModel = dataSnapshot.getValue(ChatlistModel.class);
            if (chatModel != null) {
                chatModel.setUserId(dataSnapshot.getKey());

                // Handle typing status - check both possible field names
                Boolean typingStatus = getTypingStatus(dataSnapshot);
                if (typingStatus != null) {
                    chatModel.setTyping(typingStatus);
                }
            }
            return chatModel;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing chat list item", e);
            return null;
        }
    }

    @Nullable
    private Boolean getTypingStatus(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.hasChild("isTyping")) {
            return dataSnapshot.child("isTyping").getValue(Boolean.class);
        } else if (dataSnapshot.hasChild("typing")) {
            return dataSnapshot.child("typing").getValue(Boolean.class);
        }
        return null;
    }

    private boolean isValidChatItem(@NonNull ChatlistModel chatModel) {
        return chatModel.getUserId() != null &&
                !chatModel.getUserId().equals(auth.getUid()) &&
                chatModel.getUserName() != null;
    }

    private void updateUI() {
        applyFilter();  // Apply current chip filter

        stopShimmer();

        if (filteredChatList.isEmpty()) {
            showEmptyState();
        } else {
            showChatList();
        }
    }

    private void applyFilter() {
        filteredChatList.clear();

        // Check if unread chip is checked
        if (binding.unreadChip.isChecked()) {
            // Filter for unread chats only
            for (ChatlistModel chat : chatList) {
                if (!chat.isRead()) { // Only add unread chats
                    filteredChatList.add(chat);
                }
            }
        } else {
            // "All" selected or default — copy everything
            filteredChatList.addAll(chatList);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Update UI state after filtering
        updateUIAfterFiltering();
    }

    private void updateUIAfterFiltering() {
        if (filteredChatList.isEmpty()) {
            showEmptyState();
        } else {
            showChatList();
        }
    }

    private void showShimmer() {
        if (shimmerFrameLayout != null && binding != null) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
            binding.chatListItem.setVisibility(View.GONE);
            binding.chatAnim.setVisibility(View.GONE);
        }
    }

    private void stopShimmer() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        if (binding != null) {
            binding.chatAnim.setVisibility(View.VISIBLE);
            binding.chatListItem.setVisibility(View.GONE);
        }
    }

    private void showChatList() {
        if (binding != null) {
            binding.chatAnim.setVisibility(View.GONE);
            binding.chatListItem.setVisibility(View.VISIBLE);
        }
    }

    private void handleDatabaseError(@NonNull DatabaseError error) {
        stopShimmer();
        showEmptyState();
        Log.e(TAG, "Database error: " + error.getMessage());
    }

    private void removeChatListListener() {
        if (chatListListener != null && chatListRef != null) {
            chatListRef.removeEventListener(chatListListener);
            chatListListener = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shimmerFrameLayout != null && shimmerFrameLayout.getVisibility() == View.VISIBLE) {
            shimmerFrameLayout.startShimmer();
        }

        // Reset to "All Chats" when resuming to avoid empty state
        if (binding != null) {
            binding.allChatsChip.setChecked(true);
            binding.unreadChip.setChecked(false);
            // Apply filter after setting chip state to refresh the UI
            applyFilter();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clean up listeners to prevent memory leaks
        removeChatListListener();

        // Stop shimmer animation
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
        }

        // Clear references
        binding = null;
        adapter = null;
        chatList = null;
        filteredChatList = null;
    }
}