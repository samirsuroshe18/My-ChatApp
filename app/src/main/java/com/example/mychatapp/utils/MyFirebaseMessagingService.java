package com.example.mychatapp.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mychatapp.ChatDetailActivity;
import com.example.mychatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingService";
    private DatabaseReference mDatabase;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: ");
        createNotificationChannel();

        // Extract data payload
        String userId = remoteMessage.getData().get("userId");
        String userName = remoteMessage.getData().get("userName");
        String textMessage = remoteMessage.getData().get("textMessage");
        String profilePic = remoteMessage.getData().get("profilePic");
        Log.d(TAG, "userId: "+ userId);
        Log.d(TAG, "userName: "+ userName);
        Log.d(TAG, "textMessage: "+ textMessage);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get currently open chat user (if any)
        SharedPreferences prefs = getSharedPreferences("chat_app", MODE_PRIVATE);
        String currentChatUserId = prefs.getString("currentChatUserId", null);

        // If the message is NOT from the currently open chat, show notification
        if (currentChatUserId == null || !currentChatUserId.equals(userId)) {
            showNotification(userId, userName, textMessage, profilePic);
        } else {
            Log.d("FCM", "User is already in the chat. Skipping notification.");
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Send token to your server
        sendTokenToServer(token);

        // Save token locally (optional)
//        saveTokenLocally(token);
    }

    private void sendTokenToServer(String token) {
        String userId = FirebaseAuth.getInstance().getUid();
        Log.d(TAG, "Sending token to server: " + token);

        if (userId != null) {
            mDatabase.child("Users").child(userId).child("FCMToken").setValue(token);
        }
    }

    private void showNotification(String userId, String title, String message, String profilePic) {
        // Check if notifications are allowed
        if (!PermissionUtils.canShowNotifications(this)) {
            Log.d(TAG, "Notification permission not granted, skipping notification");
            return;
        }

        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", title);
        intent.putExtra("profilePic", profilePic);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chat_channel")
                .setSmallIcon(R.drawable.ic_chat_bubble)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        // Double-check permission before showing notification
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void createNotificationChannel() {
        String channelId = "chat_channel";
        String channelName = "Chat Notifications";
        String channelDesc = "Notifications for incoming chat messages";

        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(channelDesc);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

}