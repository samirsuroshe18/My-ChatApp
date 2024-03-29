package com.example.mychatapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mychatapp.Adapters.ChatAdapter;
import com.example.mychatapp.Models.MessageModel;
import com.example.mychatapp.databinding.ActivityChatDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ChatDetailActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "Message Notification";
    private static final int NOTIFICATION_ID = 100;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ActivityChatDetailBinding binding;
    Notification notification;
    NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        Objects.requireNonNull(getSupportActionBar()).hide();


        final String senderId = auth.getUid();
        String receiverId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");

        binding.userNameChat.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.profile_pic_avatar).into(binding.profileImage);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        final ArrayList<MessageModel> messageModel = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModel, ChatDetailActivity.this, receiverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        final String senderRoom = senderId + receiverId;
        final  String receiverRoom = receiverId + senderId;

        database.getReference().child("chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageModel.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MessageModel model = dataSnapshot.getValue(MessageModel.class);
                    assert model != null;
                    model.setMessageId(dataSnapshot.getKey());
                    messageModel.add(model);
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.etMessage.getText().toString().isEmpty()) {
                    binding.etMessage.setError("Enter Message");
                    return;
                }

                    String message = binding.etMessage.getText().toString();
                    final MessageModel model = new MessageModel(senderId, message);
                    model.setTimestamp((int) new Date().getTime());
                    binding.etMessage.setText("");

                    database.getReference().child("chats").child(senderRoom).push().setValue(model)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.getReference().child("chats").child(receiverRoom).push()
                                    .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                }
                            });
                        }
                    });
            }
        });

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.notification_whatsapp_icon, null);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap largeIcon = bitmapDrawable.getBitmap();


        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.notification_whatsapp_icon)
                    .setContentTitle("New Message")
                    .setContentText("Hello World")
                    .setChannelId(CHANNEL_ID)

                    .build();
            notification = new Notification.Builder(this)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.notification_whatsapp_icon)
                    .setContentTitle("New Message")
                    .setContentText("Hello World")
                    .setChannelId(CHANNEL_ID)
                    .build();
            notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "message notification", NotificationManager.IMPORTANCE_HIGH));

        }
        else {
            Notification notification = new Notification.Builder(this)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.notification_whatsapp_icon)
                    .setContentText("New Message")
                    .setSubText("Hello World")
                    .build();
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

    }
}