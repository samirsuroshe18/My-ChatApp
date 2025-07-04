//package com.example.mychatapp;
//
//import android.content.SharedPreferences;
//import android.graphics.Rect;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewTreeObserver;
//
//import androidx.activity.EdgeToEdge;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import androidx.recyclerview.widget.DiffUtil;
//import androidx.recyclerview.widget.LinearLayoutManager;
//
//import com.example.mychatapp.Adapters.ChatAdapter;
//import com.example.mychatapp.Models.ChatItem;
//import com.example.mychatapp.Models.ChatlistModel;
//import com.example.mychatapp.Models.MessageModel;
//import com.example.mychatapp.Models.Users;
//import com.example.mychatapp.databinding.ActivityChatDetailBinding;
//import com.example.mychatapp.utils.ChatItemDiffCallback;
//import com.example.mychatapp.utils.NotificationSender;
//import com.google.android.material.appbar.MaterialToolbar;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.squareup.picasso.Picasso;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Locale;
//
//public class ChatDetailActivity extends AppCompatActivity {
//    private static final String TAG = "ChatDetailActivity";
//    FirebaseDatabase database;
//    FirebaseAuth auth;
//    ActivityChatDetailBinding binding;
//    MaterialToolbar appBar;
//    String receiverId;
//    ChatAdapter chatAdapter;
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        SharedPreferences.Editor editor = getSharedPreferences("chat_app", MODE_PRIVATE).edit();
//        editor.putString("currentChatUserId", getIntent().getStringExtra("userId")); // from intent
//        editor.apply();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        SharedPreferences.Editor editor = getSharedPreferences("chat_app", MODE_PRIVATE).edit();
//        editor.remove("currentChatUserId");
//        editor.apply();
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        appBar = findViewById(R.id.toolbar);
//        setSupportActionBar(appBar);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
//
//        database = FirebaseDatabase.getInstance();
//        auth = FirebaseAuth.getInstance();
//
//        final String senderId = auth.getUid();
//        receiverId = getIntent().getStringExtra("userId");
//        String userName = getIntent().getStringExtra("userName");
//        String profilePic = getIntent().getStringExtra("profilePic");
//        Log.d(TAG, "userId : "+receiverId);
//        Log.d(TAG, "userName : "+userName);
//        Log.d(TAG, "profilePic : "+profilePic);
//
//        binding.userNameChat.setText(userName);
//        if (profilePic != null && !profilePic.isEmpty()) {
//            Picasso.get().load(profilePic).placeholder(R.drawable.profile_pic_avatar).into(binding.profileImage);
//        }
//
//        // Set up RecyclerView
//        final ArrayList<MessageModel> messageModel = new ArrayList<>();
//        ArrayList<ChatItem> chatItems = new ArrayList<>();
//        chatAdapter = new ChatAdapter(chatItems, ChatDetailActivity.this, receiverId);
//        binding.chatRecyclerView.setAdapter(chatAdapter);
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setStackFromEnd(true); // This will show latest messages at bottom
//        binding.chatRecyclerView.setLayoutManager(layoutManager);
//
//        // Firebase chat logic
//        final String senderRoom = senderId + receiverId;
//        final String receiverRoom = receiverId + senderId;
//
//        databaseRef.child("chats").child(senderId).child(senderRoom).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // Store old list for comparison
//                ArrayList<ChatItem> oldChatItems = new ArrayList<>(chatItems);
//
//                messageModel.clear();
//                ArrayList<ChatItem> newChatItems = new ArrayList<>();
//
//                String lastDate = "";
//                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
//
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    MessageModel model = dataSnapshot.getValue(MessageModel.class);
//                    if (model == null) continue;
//
//                    model.setMessageId(dataSnapshot.getKey());
//                    messageModel.add(model);
//
//                    String currentDate = dateFormat.format(new Date(model.getTimestamp()));
//                    if (!currentDate.equals(lastDate)) {
//                        newChatItems.add(new ChatItem(getFriendlyDateHeader(model.getTimestamp())));
//                        lastDate = currentDate;
//                    }
//
//                    newChatItems.add(new ChatItem(model));
//                }
//
//                // Calculate diff and update adapter
//                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ChatItemDiffCallback(oldChatItems, newChatItems));
//
//                chatItems.clear();
//                chatItems.addAll(newChatItems);
//                diffResult.dispatchUpdatesTo(chatAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Database error: " + error.getMessage());
//            }
//        });
//
//        binding.sendBtn.setOnClickListener(v -> {
//            String messageText = binding.etMessage.getText().toString().trim();
//
//            if (messageText.isEmpty()) {
//                binding.etMessage.setError("Enter Message");
//                return;
//            }
//
//            final MessageModel model = new MessageModel(senderId, messageText);
//            model.setTimestamp(new Date().getTime());
//            binding.etMessage.setText("");
//
//            databaseRef.child("Users").child(receiverId)
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            String fcmToken = snapshot.child("fcmToken").getValue(String.class);
//                            ChatlistModel oppositeUser = snapshot.getValue(ChatlistModel.class);
//                            oppositeUser.setLastMessage(messageText);
//                            oppositeUser.setLastMsgTime(new Date().getTime());
//
//                            databaseRef.child("ChatList")
//                                    .child(receiverId)
//                                    .child(senderId)
//                                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                            ChatlistModel chatInfo = snapshot.getValue(ChatlistModel.class);
//                                            if (chatInfo != null) {
//                                                if (chatInfo.isRead()) {
//                                                    chatInfo.setRead(false);
//                                                    chatInfo.setReadCount(1);
//                                                } else {
//                                                    chatInfo.setReadCount(chatInfo.getReadCount() + 1);
//                                                }
//                                            }
//
//
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError error) {
//
//                                        }
//                                    });
//
//                            if (oppositeUser != null) {
//                                databaseRef
//                                        .child("ChatList")
//                                        .child(senderId)
//                                        .child(receiverId)
//                                        .setValue(oppositeUser)
//                                        .addOnCompleteListener(task -> {
//                                            // callback
//                                            // Sending message to the sender rooms
//                                            databaseRef.child("chats").child(senderId).child(senderRoom).push().setValue(model).addOnSuccessListener(unused -> {
//                                                // Message sent successfully
//                                                databaseRef.child("chats").child(receiverId).child(receiverRoom).push().setValue(model).addOnSuccessListener(unused1 -> {
//                                                    // Message sent successfully
//                                                    databaseRef.child("Users").child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
//                                                        @Override
//                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                                            ChatlistModel payload = snapshot.getValue(ChatlistModel.class);
//                                                            payload.setLastMessage(messageText);
//                                                            payload.setLastMsgTime(new Date().getTime());
//                                                            if (payload != null) {
//                                                                databaseRef.child("ChatList")
//                                                                        .child(receiverId)
//                                                                        .child(senderId).setValue(payload);
//                                                            }
//                                                            scrollToBottom();
//                                                            NotificationSender.sendNotification(fcmToken, payload.getUserId(), payload.getUserName(), messageText, payload.getProfilepic());
//                                                        }
//
//                                                        @Override
//                                                        public void onCancelled(@NonNull DatabaseError error) {
//
//                                                        }
//                                                    });
//                                                });
//                                            });
//                                        });
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//        });
//    }
//
//    private void scrollToBottom() {
//        if (chatAdapter.getItemCount() > 0) {
//            binding.chatRecyclerView.post(() -> {
//                binding.chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
//            });
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    public static String getFriendlyDateHeader(long timestamp) {
//        Calendar messageCal = Calendar.getInstance();
//        messageCal.setTimeInMillis(timestamp);
//
//        Calendar todayCal = Calendar.getInstance();
//
//        // Today
//        if (messageCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) && messageCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)) {
//            return "Today";
//        }
//
//        // Yesterday
//        todayCal.add(Calendar.DAY_OF_YEAR, -1);
//        if (messageCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) && messageCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)) {
//            return "Yesterday";
//        }
//
//        // Else, return formatted date
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
//        return dateFormat.format(new Date(timestamp));
//    }
//}


package com.example.mychatapp;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mychatapp.Adapters.ChatAdapter;
import com.example.mychatapp.Models.ChatItem;
import com.example.mychatapp.Models.ChatlistModel;
import com.example.mychatapp.Models.MessageModel;
import com.example.mychatapp.Models.Users;
import com.example.mychatapp.databinding.ActivityChatDetailBinding;
import com.example.mychatapp.utils.ChatItemDiffCallback;
import com.example.mychatapp.utils.NotificationSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ChatDetailActivity extends AppCompatActivity {
    private static final String TAG = "ChatDetailActivity";
    DatabaseReference databaseRef;
    FirebaseAuth auth;
    ActivityChatDetailBinding binding;
    MaterialToolbar appBar;
    String receiverId, profilePic, userName, senderId, receiverRoom, senderRoom;
    ChatAdapter chatAdapter;
    ArrayList<ChatItem> chatItems;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences.Editor editor = getSharedPreferences("chat_app", MODE_PRIVATE).edit();
        editor.putString("currentChatUserId", getIntent().getStringExtra("userId")); // from intent
        editor.apply();
        markMessagesAsRead();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences("chat_app", MODE_PRIVATE).edit();
        editor.remove("currentChatUserId");
        editor.apply();
    }

    public String getTimeAgo(long time) {
        long now = System.currentTimeMillis();
        long diff = now - time;

        if (diff < 60_000) {
            return "Just now";
        } else if (diff < 3_600_000) {
            return (diff / 60_000) + " min ago";
        } else if (diff < 86_400_000) {
            return (diff / 3_600_000) + " hrs ago";
        } else {
            return (diff / 86_400_000) + " days ago";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        appBar = findViewById(R.id.toolbar);
        setSupportActionBar(appBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        databaseRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        senderId = auth.getUid();
        receiverId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");
        profilePic = getIntent().getStringExtra("profilePic");

        binding.userNameChat.setText(userName);
        if (profilePic != null && !profilePic.isEmpty()) {
            Picasso.get().load(profilePic).placeholder(R.drawable.profile_pic_avatar).into(binding.profileImage);
        }

        // Set up RecyclerView
        chatItems = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatItems, receiverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // This will show latest messages at bottom
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        // Firebase chat logic
        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        databaseRef.child("Users").child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);

                    if (status != null) {
                        if (status.equals("online")) {
                            binding.userStatusChat.setText("Online");
                        } else if (status.equals("typing")) {
                            binding.userStatusChat.setText("Typing...");
                        } else {
                            // Convert timestamp to time ago
                            long lastSeen = Long.parseLong(status);
                            binding.userStatusChat.setText("Last seen: " + getTimeAgo(lastSeen));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        binding.etMessage.addTextChangedListener(new TextWatcher() {
//            private final Handler handler = new Handler();
//            private Runnable typingTimeout;
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                setUserTypingStatus(); // Set status to "typing"
//
//                // Remove previous timeout
//                if (typingTimeout != null) {
//                    handler.removeCallbacks(typingTimeout);
//                }
//
//                // Set a new timeout to reset status after 2.5 seconds of inactivity
//                typingTimeout = () -> setUserOnlineStatus();
//                handler.postDelayed(typingTimeout, 6000); // 2.5 seconds
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        // Also make sure your TextWatcher timeout is working properly:
        databaseRef.child("ChatList")
                .child(senderId)
                .child(receiverId)
                .child("userId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Key exists
                            String userId = dataSnapshot.getValue(String.class);
                            Log.d("Firebase", "userId exists: " + userId);

                            databaseRef.child("ChatList")
                                    .child(receiverId)
                                    .child(senderId)
                                    .child("userId")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                // Key exists
                                                String userId = dataSnapshot.getValue(String.class);
                                                Log.d("Firebase", "userId exists: " + userId);

                                                binding.etMessage.addTextChangedListener(new TextWatcher() {
                                                    private final Handler handler = new Handler();
                                                    private Runnable typingTimeout;

                                                    @Override
                                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                                                    @Override
                                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                        if (s.length() > 0) {
                                                            setUserTypingStatus(); // User is typing
                                                        }

                                                        // Remove previous timeout
                                                        if (typingTimeout != null) {
                                                            handler.removeCallbacks(typingTimeout);
                                                        }

                                                        // Set timeout to stop typing after 3 seconds of inactivity
                                                        typingTimeout = () -> setUserOnlineStatus();
                                                        handler.postDelayed(typingTimeout, 3000); // 3 seconds
                                                    }

                                                    @Override
                                                    public void afterTextChanged(Editable s) {
                                                        // If text is empty, immediately stop typing status
                                                        if (s.length() == 0) {
                                                            setUserOnlineStatus();
                                                        }
                                                    }
                                                });

                                            } else {
                                                // Key does not exist
                                                Log.d("Firebase", "userId does not exist");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e("Firebase", "Database error: " + databaseError.getMessage());
                                        }
                                    });


                        } else {
                            // Key does not exist
                            Log.d("Firebase", "userId does not exist");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Firebase", "Database error: " + databaseError.getMessage());
                    }
                });

//        databaseRef.child("chats")
//                .child(senderId)
//                .child(senderRoom)
//                        .addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                                    MessageModel message = dataSnapshot.getValue(MessageModel.class);
//
//                                    if (message != null && message.getuId().equals(receiverId) && !message.isSeen()) {
//                                        snapshot.getRef().child(dataSnapshot.getKey()).child("seen").setValue(true);
//                                    }
//                                }
//
//                                databaseRef.child("chats")
//                                        .child(receiverId)
//                                        .child(receiverRoom)
//                                        .addValueEventListener(new ValueEventListener() {
//                                            @Override
//                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                                                    MessageModel message = dataSnapshot.getValue(MessageModel.class);
//
//                                                    if (message != null && message.getuId().equals(receiverId) && !message.isSeen()) {
//                                                        snapshot.getRef().child(dataSnapshot.getKey()).child("seen").setValue(true);
//                                                    }
//                                                }
//
//                                            }
//
//                                            @Override
//                                            public void onCancelled(@NonNull DatabaseError error) {
//
//                                            }
//                                        });
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });

//        DatabaseReference messagesRef = databaseRef.child("chats")
//                .child(senderId)
//                .child(senderRoom);
//
//        messagesRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                MessageModel newMessage = snapshot.getValue(MessageModel.class);
//
//                // Check if it's a message from the other user
//                if (newMessage != null && newMessage.getuId().equals(receiverId)) {
//                    // Mark it as read immediately
////                    snapshot.getRef().child("read").setValue(true);
//                    Log.d(TAG, "onChildAdded: new message come"+newMessage.getMessage());
//                    Log.d(TAG, "onChildAdded: new message come"+newMessage.getuId());
//
//                    // Optionally update the ChatList as well
//                    markMessagesAsRead();
//                }
//            }
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        });

        databaseRef.child("ChatList").child(senderId).child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ChatlistModel chatEntry = snapshot.getValue(ChatlistModel.class);
                if (chatEntry != null && Objects.equals(chatEntry.getLastMessageBy(), receiverId)) {
                    chatEntry.setRead(true);
                    chatEntry.setReadCount(0); // Reset unread count

                    databaseRef.child("ChatList").child(senderId).child(receiverId)
                            .setValue(chatEntry).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    databaseRef.child("ChatList").child(receiverId).child(senderId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ChatlistModel chatEntry = snapshot.getValue(ChatlistModel.class);
                                            if (chatEntry != null) {
                                                chatEntry.setReadByUser(true);

                                                databaseRef.child("ChatList").child(receiverId).child(senderId)
                                                        .setValue(chatEntry);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseRef.child("chats").child(senderId).child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Store old list for comparison
                ArrayList<ChatItem> oldChatItems = new ArrayList<>(chatItems);
                ArrayList<ChatItem> newChatItems = new ArrayList<>();

                String lastDate = "";
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel model = dataSnapshot.getValue(MessageModel.class);
                    if (model == null) continue;

                    model.setMessageId(dataSnapshot.getKey());

                    String currentDate = dateFormat.format(new Date(model.getTimestamp()));
                    if (!currentDate.equals(lastDate)) {
                        newChatItems.add(new ChatItem(getFriendlyDateHeader(model.getTimestamp())));
                        lastDate = currentDate;
                    }

                    newChatItems.add(new ChatItem(model));
                }

                // Calculate diff and update adapter
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ChatItemDiffCallback(oldChatItems, newChatItems));

                chatItems.clear();
                chatItems.addAll(newChatItems);
                diffResult.dispatchUpdatesTo(chatAdapter);
                scrollToBottom();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });

        binding.sendBtn.setOnClickListener(v -> {
            String messageText = binding.etMessage.getText().toString().trim();

            if (messageText.isEmpty()) {
                binding.etMessage.setError("Enter Message");
                return;
            }
            // Send message and update chat lists
            sendMessage(messageText);
        });
    }

//    private void setUserTypingStatus() {
//        FirebaseDatabase.getInstance().getReference("Users")
//                .child(senderId)
//                .child("status")
//                .setValue("typing");
//
//        databaseRef.child("ChatList")
//                .child(receiverId)
//                .child(senderId)
//                .child("isTyping").setValue(true);
//    }
//
//    private void setUserOnlineStatus() {
//        FirebaseDatabase.getInstance().getReference("Users")
//                .child(senderId)
//                .child("status")
//                .setValue("online");
//
//        databaseRef.child("ChatList")
//                .child(receiverId)
//                .child(senderId)
//                .child("isTyping").setValue(false);
//    }

    private void setUserTypingStatus() {


        FirebaseDatabase.getInstance().getReference("Users")
                .child(senderId)
                .child("status")
                .setValue("typing");

        // Update typing status in ChatList with proper field name
        databaseRef.child("ChatList")
                .child(receiverId)
                .child(senderId)
                .child("isTyping").setValue(true)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Typing status set to true"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to set typing status: " + e.getMessage()));
    }

    private void setUserOnlineStatus() {
        FirebaseDatabase.getInstance().getReference("Users")
                .child(senderId)
                .child("status")
                .setValue("online");

        // Update typing status in ChatList
        databaseRef.child("ChatList")
                .child(receiverId)
                .child(senderId)
                .child("isTyping").setValue(false)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Typing status set to false"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to set typing status: " + e.getMessage()));
    }



    private void sendMessage(String messageText) {
        // First, send the message to both chat rooms
        databaseRef.child("chats").child(senderId).child(senderRoom).push().setValue(new MessageModel(senderId, messageText, new Date().getTime(), false))
                .addOnSuccessListener(unused -> databaseRef.child("chats").child(receiverId).child(receiverRoom).push().setValue(new MessageModel(senderId, messageText, new Date().getTime(), false))
                        .addOnSuccessListener(unused1 -> {
                            // Message sent successfully, now update chat lists
                            updateChatLists(messageText);
                            binding.etMessage.setText("");
//                            scrollToBottom();
                        }));
    }

    private void updateChatLists(String messageText) {
        long currentTime = new Date().getTime();

        // Get sender's info to update receiver's chat list
        databaseRef.child("Users").child(senderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users senderUser = snapshot.getValue(Users.class);
                        if (senderUser != null) {
                            // Create/Update receiver's chat list entry
                            ChatlistModel receiverChatEntry = new ChatlistModel();
                            receiverChatEntry.setUserId(senderId);
                            receiverChatEntry.setLastMessageBy(senderId);
                            receiverChatEntry.setUserName(senderUser.getUserName());
                            receiverChatEntry.setProfilepic(senderUser.getProfilepic());
                            receiverChatEntry.setLastMessage(messageText);
                            receiverChatEntry.setLastMsgTime(currentTime);
                            receiverChatEntry.setReadByUser(true);
                            receiverChatEntry.setReadCount(0);
                            receiverChatEntry.setRead(false);

                            // Check if chat entry exists and update unread count
                            databaseRef.child("ChatList").child(receiverId).child(senderId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ChatlistModel existingChat = snapshot.getValue(ChatlistModel.class);
                                            if (existingChat != null) {
                                                receiverChatEntry.setReadCount(existingChat.getReadCount() + 1);
                                            } else {
                                                receiverChatEntry.setReadCount(1);
                                            }

                                            // Save to receiver's chat list
                                            databaseRef.child("ChatList").child(receiverId).child(senderId)
                                                    .setValue(receiverChatEntry);

                                            // Send notification
                                            databaseRef.child("Users").child(receiverId)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            String fcmToken = snapshot.child("FCMToken").getValue(String.class);
                                                            if (fcmToken != null) {
                                                                NotificationSender.sendNotification(fcmToken, senderId, senderUser.getUserName(), messageText, senderUser.getProfilepic());
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {}
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Get receiver's info to update sender's chat list
        databaseRef.child("Users").child(receiverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users receiverUser = snapshot.getValue(Users.class);
                        if (receiverUser != null) {
                            // Create/Update sender's chat list entry
                            ChatlistModel senderChatEntry = new ChatlistModel();
                            senderChatEntry.setUserId(receiverId);
                            senderChatEntry.setLastMessageBy(senderId);
                            senderChatEntry.setUserName(receiverUser.getUserName());
                            senderChatEntry.setProfilepic(receiverUser.getProfilepic());
                            senderChatEntry.setLastMessage(messageText);
                            senderChatEntry.setLastMsgTime(currentTime);
                            senderChatEntry.setReadByUser(false);
                            senderChatEntry.setRead(true); // Sender has read their own message
                            senderChatEntry.setReadCount(0); // Reset unread count for sender

                            // Save to sender's chat list
                            databaseRef.child("ChatList").child(senderId).child(receiverId)
                                    .setValue(senderChatEntry);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void markMessagesAsRead() {
        if (senderId != null && receiverId != null) {
            // Mark the chat as read in current user's chat list
            databaseRef.child("ChatList").child(senderId).child(receiverId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ChatlistModel chatEntry = snapshot.getValue(ChatlistModel.class);
                            if (chatEntry != null) {
                                chatEntry.setRead(true);
                                chatEntry.setReadByUser(true);
                                chatEntry.setReadCount(0); // Reset unread count

                                databaseRef.child("ChatList").child(senderId).child(receiverId)
                                        .setValue(chatEntry);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error marking messages as read: " + error.getMessage());
                        }
                    });

            // Mark the chat as read in current user's chat list
            databaseRef.child("ChatList").child(receiverId).child(senderId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ChatlistModel chatEntry = snapshot.getValue(ChatlistModel.class);
                            if (chatEntry != null) {
                                chatEntry.setReadByUser(true);

                                databaseRef.child("ChatList").child(receiverId).child(senderId)
                                        .setValue(chatEntry);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error marking messages as read: " + error.getMessage());
                        }
                    });
        }
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            binding.chatRecyclerView.post(() -> binding.chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getFriendlyDateHeader(long timestamp) {
        Calendar messageCal = Calendar.getInstance();
        messageCal.setTimeInMillis(timestamp);

        Calendar todayCal = Calendar.getInstance();

        // Today
        if (messageCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) && messageCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)) {
            return "Today";
        }

        // Yesterday
        todayCal.add(Calendar.DAY_OF_YEAR, -1);
        if (messageCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) && messageCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }

        // Else, return formatted date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }
}