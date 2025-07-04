package com.example.mychatapp.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mychatapp.ChatDetailActivity;
import com.example.mychatapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingService";

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

    private void showNotification(String userId, String title, String message, String profilePic) {
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

}


