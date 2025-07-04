//package com.example.mychatapp.Fragments;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.example.mychatapp.Adapters.UsersAdapter;
//import com.example.mychatapp.Models.ChatlistModel;
//import com.example.mychatapp.SelectContactActivity;
//import com.example.mychatapp.databinding.FragmentChatListBinding;
//import com.facebook.shimmer.ShimmerFrameLayout;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//
//public class HomeFragment extends Fragment {
//    public static final String TAG = "HomeFragment";
//    private FragmentChatListBinding binding;
//    FirebaseAuth auth;
//    FirebaseDatabase database;
//    ArrayList<ChatlistModel> list;
//    UsersAdapter adapter;
//    LinearLayoutManager layoutManager;
//    private ShimmerFrameLayout shimmerFrameLayout;
//
//    public HomeFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        binding = FragmentChatListBinding.inflate(inflater, container, false);
//
//        // Initialize shimmer
//        shimmerFrameLayout = binding.shimmerChatContainer;
//
//        auth = FirebaseAuth.getInstance();
//        database = FirebaseDatabase.getInstance();
//        list = new ArrayList<>();
//        layoutManager = new LinearLayoutManager(getContext());
//        adapter = new UsersAdapter(list, getContext());
//        binding.chatListItem.setAdapter(adapter);
//        binding.chatListItem.setLayoutManager(layoutManager);
//
//        // Start shimmer effect
//        startShimmer();
//
//        // Load users from Firebase
//        loadUsers();
//
//        binding.selectContact.setOnClickListener(v -> {
//            Intent intent = new Intent(getContext(), SelectContactActivity.class);
//            startActivity(intent);
//        });
//
//        return binding.getRoot();
//    }
//
//    private void startShimmer() {
//        if (shimmerFrameLayout != null) {
//            shimmerFrameLayout.setVisibility(View.VISIBLE);
//            shimmerFrameLayout.startShimmer();
//            binding.scrollView.setVisibility(View.GONE);
//        }
//    }
//
//    private void stopShimmer() {
//        if (shimmerFrameLayout != null) {
//            shimmerFrameLayout.stopShimmer();
//            shimmerFrameLayout.setVisibility(View.GONE);
//            binding.scrollView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void loadUsers() {
//        database.getReference().child("ChatList").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                list.clear();
//
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                    ChatlistModel users = dataSnapshot.getValue(ChatlistModel.class);
//                    if (users != null) {
//                        users.setUserId(dataSnapshot.getKey());
//                        Log.d(TAG, "userId :"+users.getUserId());
//                        Log.d(TAG, "profile :"+users.getProfilepic());
//                        Log.d(TAG, "userName :"+users.getUserName());
//                        Log.d(TAG, "lastMsgTime :"+users.getLastMsgTime());
//                        Log.d(TAG, "isRead :"+users.isRead());
//
//                        if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())){
//                            list.add(users);
//                        }
//                    }
//                }
//                adapter.notifyDataSetChanged();
//
//                // Stop shimmer and show RecyclerView
//                stopShimmer();
//
//                // ✅ Show animation if list is empty
//                if (list.isEmpty()) {
//                    binding.chatAnim.setVisibility(View.VISIBLE);
//                    binding.chatListItem.setVisibility(View.GONE);
//                } else {
//                    binding.chatAnim.setVisibility(View.GONE);
//                    binding.chatListItem.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Stop shimmer even if there's an error
//                stopShimmer();
//            }
//        });
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        // Resume shimmer if it's currently visible
//        if (shimmerFrameLayout != null && shimmerFrameLayout.getVisibility() == View.VISIBLE) {
//            shimmerFrameLayout.startShimmer();
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        // Pause shimmer to save resources
//        if (shimmerFrameLayout != null) {
//            shimmerFrameLayout.stopShimmer();
//        }
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        // Stop shimmer and clean up
//        if (shimmerFrameLayout != null) {
//            shimmerFrameLayout.stopShimmer();
//        }
//        binding = null;
//    }
//}


//package com.example.mychatapp.Fragments;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.example.mychatapp.Adapters.UsersAdapter;
//import com.example.mychatapp.Models.ChatlistModel;
//import com.example.mychatapp.SelectContactActivity;
//import com.example.mychatapp.databinding.FragmentChatListBinding;
//import com.facebook.shimmer.ShimmerFrameLayout;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//
//public class HomeFragment extends Fragment {
//    public static final String TAG = "HomeFragment";
//    private FragmentChatListBinding binding;
//    FirebaseAuth auth;
//    FirebaseDatabase database;
//    ArrayList<ChatlistModel> list;
//    UsersAdapter adapter;
//    LinearLayoutManager layoutManager;
//    private ShimmerFrameLayout shimmerFrameLayout;
//    private ValueEventListener chatListListener; // Store the listener reference
//
//    public HomeFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        binding = FragmentChatListBinding.inflate(inflater, container, false);
//
//        // Initialize shimmer
//        shimmerFrameLayout = binding.shimmerChatContainer;
//
//        auth = FirebaseAuth.getInstance();
//        database = FirebaseDatabase.getInstance();
//        list = new ArrayList<>();
//        layoutManager = new LinearLayoutManager(getContext());
//        adapter = new UsersAdapter(list, getContext());
//        binding.chatListItem.setAdapter(adapter);
//        binding.chatListItem.setLayoutManager(layoutManager);
//
//        // Start shimmer effect
//        startShimmer();
//
//        // Load users from Firebase
//        loadUsers();
//
//        binding.selectContact.setOnClickListener(v -> {
//            Intent intent = new Intent(getContext(), SelectContactActivity.class);
//            startActivity(intent);
//        });
//
//        return binding.getRoot();
//    }
//
//    private void startShimmer() {
//        if (shimmerFrameLayout != null) {
//            shimmerFrameLayout.setVisibility(View.VISIBLE);
//            shimmerFrameLayout.startShimmer();
//            binding.scrollView.setVisibility(View.GONE);
//        }
//    }
//
//    private void stopShimmer() {
//        if (shimmerFrameLayout != null) {
//            shimmerFrameLayout.stopShimmer();
//            shimmerFrameLayout.setVisibility(View.GONE);
//            binding.scrollView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void loadUsers() {
//        // Remove previous listener if exists
//        if (chatListListener != null) {
//            database.getReference().child("ChatList").child(auth.getUid()).removeEventListener(chatListListener);
//        }
//
//        chatListListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                list.clear();
//
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    ChatlistModel users = dataSnapshot.getValue(ChatlistModel.class);
//                    Log.d(TAG, "onDataChange: User is typing : "+users.isTyping());
//                    if (users != null) {
//                        users.setUserId(dataSnapshot.getKey());
//                        Log.d(TAG, "userId :" + users.getUserId());
//                        Log.d(TAG, "profile :" + users.getProfilepic());
//                        Log.d(TAG, "userName :" + users.getUserName());
//                        Log.d(TAG, "lastMsgTime :" + users.getLastMsgTime());
//                        Log.d(TAG, "isRead :" + users.isRead());
//
//                        if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
//                            list.add(users);
//                        }
//                    }
//                }
//                adapter.notifyDataSetChanged();
//
//                // Stop shimmer and show RecyclerView
//                stopShimmer();
//
//                // Show animation if list is empty
//                if (list.isEmpty()) {
//                    binding.chatAnim.setVisibility(View.VISIBLE);
//                    binding.chatListItem.setVisibility(View.GONE);
//                } else {
//                    binding.chatAnim.setVisibility(View.GONE);
//                    binding.chatListItem.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Stop shimmer even if there's an error
//                stopShimmer();
//                Log.e(TAG, "Firebase error: " + error.getMessage());
//            }
//        };
//
//        database.getReference().child("ChatList").child(auth.getUid()).addValueEventListener(chatListListener);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d(TAG, "onResume called - refreshing chat list");
//
//        // Resume shimmer if it's currently visible
//        if (shimmerFrameLayout != null && shimmerFrameLayout.getVisibility() == View.VISIBLE) {
//            shimmerFrameLayout.startShimmer();
//        }
//
//        // Refresh the data when returning from chat
//        if (auth != null && database != null) {
//            loadUsers(); // This will refresh the entire list
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        // Pause shimmer to save resources
//        if (shimmerFrameLayout != null) {
//            shimmerFrameLayout.stopShimmer();
//        }
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        // Remove the listener to prevent memory leaks
//        if (chatListListener != null && auth != null) {
//            database.getReference().child("ChatList").child(auth.getUid()).removeEventListener(chatListListener);
//        }
//
//        // Stop shimmer and clean up
//        if (shimmerFrameLayout != null) {
//            shimmerFrameLayout.stopShimmer();
//        }
//        binding = null;
//    }
//}



package com.example.mychatapp.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";
    private FragmentChatListBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<ChatlistModel> list;
    UsersAdapter adapter;
    LinearLayoutManager layoutManager;
    private ShimmerFrameLayout shimmerFrameLayout;
    private ValueEventListener chatListListener; // Store the listener reference

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);

        shimmerFrameLayout = binding.shimmerChatContainer;

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        list = new ArrayList<>();
        layoutManager = new LinearLayoutManager(getContext());
        adapter = new UsersAdapter(list, getContext());
        binding.chatListItem.setAdapter(adapter);
        binding.chatListItem.setLayoutManager(layoutManager);

        startShimmer();
        loadUsers();

        binding.selectContact.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SelectContactActivity.class);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    private void startShimmer() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
            binding.scrollView.setVisibility(View.GONE);
        }
    }

    private void stopShimmer() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            binding.scrollView.setVisibility(View.VISIBLE);
        }
    }

    private void loadUsers() {
        if (chatListListener != null) {
            database.getReference().child("ChatList").child(auth.getUid()).removeEventListener(chatListListener);
        }

        chatListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "ChatList data changed - total children: " + snapshot.getChildrenCount());

                list.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        ChatlistModel users = dataSnapshot.getValue(ChatlistModel.class);
                        if (users != null) {
                            users.setUserId(dataSnapshot.getKey());

                            // Debug logs
                            Log.d(TAG, "=== User Data ===");
                            Log.d(TAG, "UserId: " + users.getUserId());
                            Log.d(TAG, "UserName: " + users.getUserName());
                            Log.d(TAG, "isTyping: " + users.isTyping());
                            Log.d(TAG, "Raw isTyping from snapshot: " + dataSnapshot.child("isTyping").getValue());
                            Log.d(TAG, "Raw typing from snapshot: " + dataSnapshot.child("typing").getValue());
                            Log.d(TAG, "================");

                            // Check both possible field names for typing
                            Boolean typingValue = null;
                            if (dataSnapshot.hasChild("isTyping")) {
                                typingValue = dataSnapshot.child("isTyping").getValue(Boolean.class);
                            } else if (dataSnapshot.hasChild("typing")) {
                                typingValue = dataSnapshot.child("typing").getValue(Boolean.class);
                            }

                            if (typingValue != null) {
                                users.setTyping(typingValue);
                                Log.d(TAG, "Set typing status to: " + typingValue + " for user: " + users.getUserName());
                            }

                            if (!users.getUserId().equals(auth.getUid())) {
                                list.add(users);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing chat list item: " + e.getMessage());
                    }
                }

                adapter.notifyDataSetChanged();
                stopShimmer();

                if (list.isEmpty()) {
                    binding.chatAnim.setVisibility(View.VISIBLE);
                    binding.chatListItem.setVisibility(View.GONE);
                } else {
                    binding.chatAnim.setVisibility(View.GONE);
                    binding.chatListItem.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                stopShimmer();
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        };

        database.getReference().child("ChatList").child(auth.getUid())
                .addValueEventListener(chatListListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        if (shimmerFrameLayout != null && shimmerFrameLayout.getVisibility() == View.VISIBLE) {
            shimmerFrameLayout.startShimmer();
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
        if (chatListListener != null && auth != null) {
            database.getReference().child("ChatList").child(auth.getUid()).removeEventListener(chatListListener);
        }

        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
        }
        binding = null;
    }
}