package com.example.mychatapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatDetailActivity extends AppCompatActivity {
    private static final String TAG = "ChatDetailActivity";
    private static final long TYPING_TIMEOUT = 3000; // 3 seconds
    private static final String PREFS_NAME = "chat_app";
    private static final String CURRENT_CHAT_USER_ID = "currentChatUserId";

    // Firebase references
    private DatabaseReference databaseRef;
    private DatabaseReference usersRef;
    private DatabaseReference chatsRef;
    private DatabaseReference chatListRef;
    private FirebaseAuth auth;

    // UI Components
    private ActivityChatDetailBinding binding;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatItem> chatItems;

    // Chat data
    private String receiverId, profilePic, userName, senderId, receiverRoom, senderRoom;

    // Listeners for cleanup
    private ValueEventListener userStatusListener;
    private ValueEventListener chatMessagesListener;
    private ValueEventListener markReadListener;
    private ValueEventListener chatListListener;
    private ValueEventListener senderChatListListener;
    private ValueEventListener receiverChatListListener;
    // Handler for typing status
    private final Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingTimeout;

    // SharedPreferences
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setupToolbar();
        extractIntentData();
        setupRecyclerView();
        setupFirebaseReferences();
        setupUI();
        setupListeners();
    }

    private void initializeComponents() {
        auth = FirebaseAuth.getInstance();
        senderId = auth.getUid();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        chatItems = new ArrayList<>();

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.moreVert.setOnClickListener(v -> showPopupMenu(binding.moreVert));
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(chatItems, receiverId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        binding.chatRecyclerView.setAdapter(chatAdapter);
    }

    private void extractIntentData() {
        receiverId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");
        profilePic = getIntent().getStringExtra("profilePic");

        if (receiverId == null || senderId == null) {
            Log.e(TAG, "Missing required user IDs");
            finish();
            return;
        }

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;
    }

    private void setupFirebaseReferences() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        usersRef = databaseRef.child("Users");
        chatsRef = databaseRef.child("chats");
        chatListRef = databaseRef.child("ChatList");
    }

    private void setupUI() {
        binding.userNameChat.setText(userName);
        loadProfileImage();
    }

    private void loadProfileImage() {
        if (profilePic != null && !profilePic.isEmpty()) {
            Picasso.get()
                    .load(profilePic)
                    .placeholder(R.drawable.profile_pic_avatar)
                    .error(R.drawable.profile_pic_avatar)
                    .into(binding.profileImage);
        }
    }

    private void setupListeners() {
        setupUserStatusListener();
        setupChatMessagesListener();
        setupChatListListener();
        setupTypingListener();
        setupSendButton();
    }

    private void setupUserStatusListener() {
        userStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object status = snapshot.child("status").getValue();
                    Log.d(TAG, "User status Type listener: " + status.getClass());
                    if(status instanceof Number){
                        Log.d(TAG, "User status listener Long: " + status);
                        updateUserStatus(getTimeAgo((Long) status));
                    }else {
                        Log.d(TAG, "User status listener: " + status);
                        updateUserStatus((String) status);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "User status listener error: " + error.getMessage());
            }
        };
        usersRef.child(receiverId).addValueEventListener(userStatusListener);
    }

    private void updateUserStatus(String status) {
        Log.d(TAG, "Updating user status: " + status);
        if (status == null) return;

        switch (status) {
            case "online":
                binding.userStatusChat.setText("Online");
                break;
            case "typing":
                binding.userStatusChat.setText("Typing...");
                break;
            default:
                try {
                    if(status!=null){
                        binding.userStatusChat.setText("Last seen: " + status);
                    }else{
                        long lastSeen = Long.parseLong(status);
                        binding.userStatusChat.setText("Last seen: " + getTimeAgo(lastSeen));
                    }
                } catch (NumberFormatException e) {
                    binding.userStatusChat.setText("Offline");
                }
                break;
        }
    }

    private void setupChatMessagesListener() {
        chatMessagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateChatMessages(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Chat messages listener error: " + error.getMessage());
            }
        };
        chatsRef.child(senderId).child(senderRoom).addValueEventListener(chatMessagesListener);
    }

    private void updateChatMessages(DataSnapshot snapshot) {
        List<ChatItem> oldChatItems = new ArrayList<>(chatItems);
        List<ChatItem> newChatItems = new ArrayList<>();

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

        // Use DiffUtil for efficient updates
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new ChatItemDiffCallback(oldChatItems, newChatItems));

        chatItems.clear();
        chatItems.addAll(newChatItems);
        diffResult.dispatchUpdatesTo(chatAdapter);

        scrollToBottom();
    }

    private void setupChatListListener() {
        chatListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ChatlistModel chatEntry = snapshot.getValue(ChatlistModel.class);
                if (chatEntry != null && receiverId.equals(chatEntry.getLastMessageBy())) {
                    markChatAsRead(chatEntry);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Chat list listener error: " + error.getMessage());
            }
        };
        chatListRef.child(senderId).child(receiverId).child("userId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            checkReceiverChatListForRead(); // <- NEW
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Chat list listener error: " + error.getMessage());
                    }
                });
//        chatListRef.child(senderId).child(receiverId).addValueEventListener(chatListListener);
    }

    private void checkReceiverChatListForRead() {
        chatListRef.child(receiverId).child(senderId).child("userId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Now add the real chatListListener
                            chatListRef.child(senderId).child(receiverId).addValueEventListener(chatListListener);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Receiver chat check for read failed: " + error.getMessage());
                    }
                });
    }


    private void markChatAsRead(ChatlistModel chatEntry) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("read", true);
        updates.put("readCount", 0);

        // Update sender's chat list
        chatListRef.child(senderId).child(receiverId).updateChildren(updates);

        // Update receiver's chat list
        Map<String, Object> receiverUpdates = new HashMap<>();
        receiverUpdates.put("readByUser", true);
        receiverUpdates.put("seen", true);
        chatListRef.child(receiverId).child(senderId).updateChildren(receiverUpdates);
    }

    private void setupTypingListener() {
        // Check if both users exist in each other's chat list before enabling typing
        chatListRef.child(senderId).child(receiverId).child("userId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            checkReceiverChatList();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Typing check error: " + error.getMessage());
                    }
                });
    }

    private void checkReceiverChatList() {
        chatListRef.child(receiverId).child(senderId).child("userId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            setupTypingDetection();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Receiver chat list check error: " + error.getMessage());
                    }
                });
    }

    private void setupTypingDetection() {
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    setUserTypingStatus();
                }

                // Cancel previous timeout
                if (typingTimeout != null) {
                    typingHandler.removeCallbacks(typingTimeout);
                }

                // Set new timeout
                typingTimeout = this::stopTyping;
                typingHandler.postDelayed(typingTimeout, TYPING_TIMEOUT);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    stopTyping();
                }
            }

            private void stopTyping() {
                setUserOnlineStatus();
                if (typingTimeout != null) {
                    typingHandler.removeCallbacks(typingTimeout);
                    typingTimeout = null;
                }
            }
        });
    }

    private void setupSendButton() {
        binding.sendBtn.setOnClickListener(v -> {
            String messageText = binding.etMessage.getText().toString().trim();
            if (messageText.isEmpty()) {
                binding.etMessage.setError("Enter Message");
                return;
            }
            sendMessage(messageText);
        });
    }

    private void setUserTypingStatus() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "typing");
        usersRef.child(senderId).updateChildren(updates);

        // Update typing status in chat list
        chatListRef.child(receiverId).child(senderId).child("isTyping").setValue(true);
    }

    private void setUserOnlineStatus() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "online");
        usersRef.child(senderId).updateChildren(updates);

        // Update typing status in chat list
        chatListRef.child(receiverId).child(senderId).child("isTyping").setValue(false);
    }

    private void sendMessage(String messageText) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("message", messageText);
        messageData.put("timestamp", ServerValue.TIMESTAMP);
        messageData.put("read", false);
        messageData.put("seen", false);
        Log.d(TAG, "Message data: " + messageData);

        // Use batch write for better performance
        Map<String, Object> updates = new HashMap<>();
        String messageKey = chatsRef.child(senderId).child(senderRoom).push().getKey();

        if (messageKey != null) {
            updates.put("chats/" + senderId + "/" + senderRoom + "/" + messageKey, messageData);
            updates.put("chats/" + receiverId + "/" + receiverRoom + "/" + messageKey, messageData);

            databaseRef.updateChildren(updates)
                    .addOnSuccessListener(unused -> {
                        updateChatLists(messageText);
                        binding.etMessage.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to send message: " + e.getMessage());
                        // Show error to user
                    });
        }
    }

    private void updateChatLists(String messageText) {
        long currentTime = System.currentTimeMillis();

        // Update both chat lists efficiently
        updateSenderChatList(messageText, currentTime);
        updateReceiverChatList(messageText, currentTime);
    }

    private void updateSenderChatList(String messageText, long currentTime) {
        usersRef.child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("userName").getValue(String.class);
                String profilepic = snapshot.child("profilepic").getValue(String.class);
                if (userName != null && profilepic != null) {
                    ChatlistModel senderChatEntry = createChatListEntry(
                            receiverId, userName, profilepic,
                            messageText, currentTime, senderId, true, false, 0);

                    chatListRef.child(senderId).child(receiverId).setValue(senderChatEntry);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error updating sender chat list: " + error.getMessage());
            }
        });
    }

    private void updateReceiverChatList(String messageText, long currentTime) {
        usersRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("userName").getValue(String.class);
                String profilepic = snapshot.child("profilepic").getValue(String.class);
                if (userName != null && profilepic != null) {
                    // Check existing unread count
                    chatListRef.child(receiverId).child(senderId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    long unreadCount = 1;
                                    ChatlistModel existingChat = snapshot.getValue(ChatlistModel.class);
                                    if (existingChat != null) {
                                        unreadCount = existingChat.getReadCount() + 1;
                                    }

                                    ChatlistModel receiverChatEntry = createChatListEntry(
                                            senderId, userName, profilepic,
                                            messageText, currentTime, senderId, false, true, unreadCount);

                                    chatListRef.child(receiverId).child(senderId).setValue(receiverChatEntry);

                                    // Send notification
                                    sendNotification(userName, profilepic, messageText);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Error updating receiver chat list: " + error.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error getting sender info: " + error.getMessage());
            }
        });
    }

    private ChatlistModel createChatListEntry(String userId, String userName, String profilePic,
                                              String lastMessage, long timestamp, String lastMessageBy,
                                              boolean read, boolean readByUser, long readCount) {
        ChatlistModel chatEntry = new ChatlistModel();
        chatEntry.setUserId(userId);
        chatEntry.setUserName(userName);
        chatEntry.setProfilepic(profilePic);
        chatEntry.setLastMessage(lastMessage);
        chatEntry.setLastMsgTime(timestamp);
        chatEntry.setLastMessageBy(lastMessageBy);
        chatEntry.setRead(read);
        chatEntry.setReadByUser(readByUser);
        chatEntry.setReadCount(readCount);
        return chatEntry;
    }

    private void sendNotification(String userName, String profilePic, String messageText) {
        usersRef.child(receiverId).child("FCMToken")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String fcmToken = snapshot.getValue(String.class);
                        if (fcmToken != null) {
                            NotificationSender.sendNotification(fcmToken, senderId,
                                    userName, messageText, profilePic);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting FCM token: " + error.getMessage());
                    }
                });
    }

    private void markMessagesAsRead() {
        if (senderId == null || receiverId == null) return;

        DatabaseReference senderRef = chatListRef.child(senderId).child(receiverId);
        DatabaseReference receiverRef = chatListRef.child(receiverId).child(senderId);

        // Remove any existing listeners (defensive)
        if (senderChatListListener != null) {
            senderRef.removeEventListener(senderChatListListener);
        }

        if (receiverChatListListener != null) {
            receiverRef.removeEventListener(receiverChatListListener);
        }

        // Set sender listener
        senderChatListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("read", true);
                    updates.put("readByUser", true);
                    updates.put("readCount", 0);
                    senderRef.updateChildren(updates);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        senderRef.addValueEventListener(senderChatListListener);

        // Set receiver listener
        receiverChatListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("readByUser", true);
                    receiverRef.updateChildren(updates);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        receiverRef.addValueEventListener(receiverChatListListener);
    }

    private void markMessagesAsSeen() {
        markReadListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateSeenMessage(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Chat messages listener error: " + error.getMessage());
            }
        };

        chatsRef.child(receiverId).child(receiverRoom).addValueEventListener(markReadListener);
    }

    private void updateSeenMessage(DataSnapshot snapshot) {
        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            MessageModel message = dataSnapshot.getValue(MessageModel.class);
            if (message == null) continue;

            boolean isIncoming = !senderId.equals(message.getSenderId());
            boolean notSeen = !Boolean.TRUE.equals(message.isSeen());

            if (isIncoming && notSeen) {
                dataSnapshot.getRef().child("seen").setValue(true);
            }
        }
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            binding.chatRecyclerView.post(() ->
                    binding.chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences.edit()
                .putString(CURRENT_CHAT_USER_ID, receiverId)
                .apply();
        markMessagesAsRead();
        markMessagesAsSeen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.edit()
                .remove(CURRENT_CHAT_USER_ID)
                .apply();
        setUserOnlineStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanup();
    }

    private void cleanup() {
        // Remove Firebase listeners
        if (userStatusListener != null && receiverId != null) {
            usersRef.child(receiverId).removeEventListener(userStatusListener);
        }
        if (chatMessagesListener != null && senderId != null && senderRoom != null) {
            chatsRef.child(senderId).child(senderRoom).removeEventListener(chatMessagesListener);
        }
        if (markReadListener != null && receiverId != null && receiverRoom != null) {
            chatsRef.child(receiverId).child(receiverRoom).removeEventListener(markReadListener);
        }
        if (chatListListener != null && senderId != null && receiverId != null) {
            chatListRef.child(senderId).child(receiverId).removeEventListener(chatListListener);
        }
        if (senderChatListListener != null && senderId != null && receiverId != null) {
            chatListRef.child(senderId).child(receiverId).removeEventListener(senderChatListListener);
            senderChatListListener = null;
        }
        if (receiverChatListListener != null && senderId != null && receiverId != null) {
            chatListRef.child(receiverId).child(senderId).removeEventListener(receiverChatListListener);
            receiverChatListListener = null;
        }

        // Clean up handler
        if (typingTimeout != null) {
            typingHandler.removeCallbacks(typingTimeout);
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

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu()); // menu with only Clear Chat
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear_chat) {
                showClearChatConfirmation(); // your method
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showClearChatConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("Are you sure you want to delete all messages in this chat?")
                .setPositiveButton("Clear", (dialog, which) -> clearAllMessages())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllMessages() {
        if (senderId == null || receiverId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("/" + senderId + "/" + senderRoom, null);

        chatsRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Chat cleared", Toast.LENGTH_SHORT).show();
                    updateLastMessage(senderId, senderRoom, receiverRoom);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to clear chat", Toast.LENGTH_SHORT).show();
                    Log.e("ClearChat", "Error clearing chat", e);
                });
    }

    private void updateLastMessage(String userId, String senderRoom, String receiverRoom) {
        // Get the last message from sender's chat
        chatsRef.child(userId)
                .child(senderRoom)
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String lastMessage = "";

                        for (DataSnapshot messageSnap : snapshot.getChildren()) {
                            String message = messageSnap.child("message").getValue(String.class);
                            if (message != null) {
                                lastMessage = message;
                            }
                        }

                        // Update ChatList for sender
                        updateChatList(userId, receiverId, lastMessage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to get last message", error.toException());
                    }
                });
    }

    private void updateChatList(String userId, String chatPartnerId, String lastMessage) {
        if (userId == null || chatPartnerId == null) {
            Log.e(TAG, "updateChatList failed: userId or chatPartnerId is null. userId=" + userId + ", chatId=" + chatPartnerId);
            return;
        }

        chatListRef.child(userId)
                .child(chatPartnerId)
                .child("lastMessage")
                .setValue(lastMessage)
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to update ChatList", e)
                );
    }

    private String getTimeAgo(long time) {
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

    public static String getFriendlyDateHeader(long timestamp) {
        Calendar messageCal = Calendar.getInstance();
        messageCal.setTimeInMillis(timestamp);

        Calendar todayCal = Calendar.getInstance();

        // Today
        if (messageCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                messageCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)) {
            return "Today";
        }

        // Yesterday
        todayCal.add(Calendar.DAY_OF_YEAR, -1);
        if (messageCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                messageCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }

        // Else, return formatted date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }
}