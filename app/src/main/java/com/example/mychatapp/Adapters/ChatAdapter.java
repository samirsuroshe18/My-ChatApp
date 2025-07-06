package com.example.mychatapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mychatapp.Models.ChatItem;
import com.example.mychatapp.Models.MessageModel;
import com.example.mychatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ChatAdapter";
    private static final int SENDER_VIEW_TYPE = 1;
    private static final int RECEIVER_VIEW_TYPE = 2;
    private static final int DATE_VIEW_TYPE = 3;

    private final String recId;
    private final List<ChatItem> chatItems;
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;
    private final SimpleDateFormat timeFormatter;

    // Interface for handling delete operations
    public interface OnMessageDeleteListener {
        void onMessageDeleted(String messageId);
        void onDeleteError(String error);
    }

    private OnMessageDeleteListener deleteListener;

    public ChatAdapter(List<ChatItem> chatItems, String recId) {
        Log.d(TAG, "Receiver ID : "+recId);
        this.chatItems = chatItems;
        this.recId = recId;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.timeFormatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    }

    public void setOnMessageDeleteListener(OnMessageDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case SENDER_VIEW_TYPE:
                return new SenderViewHolder(inflater.inflate(R.layout.sample_sender, parent, false));
            case RECEIVER_VIEW_TYPE:
                return new ReceiverViewHolder(inflater.inflate(R.layout.sample_reciever, parent, false));
            case DATE_VIEW_TYPE:
                return new DateViewHolder(inflater.inflate(R.layout.item_date_header, parent, false));
            default:
                throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatItem item = chatItems.get(position);
        if (item.getType() == ChatItem.TYPE_DATE) {
            return DATE_VIEW_TYPE;
        }

        MessageModel msg = item.getMessageModel();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null && msg.getSenderId().equals(currentUser.getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem item = chatItems.get(position);

        if (holder instanceof DateViewHolder) {
            bindDateViewHolder((DateViewHolder) holder, item);
        } else {
            MessageModel msg = item.getMessageModel();
            setupMessageLongClickListener(holder, msg, position, holder.itemView.getContext());

            if (holder instanceof SenderViewHolder) {
                bindSenderViewHolder((SenderViewHolder) holder, msg);
            } else if (holder instanceof ReceiverViewHolder) {
                bindReceiverViewHolder((ReceiverViewHolder) holder, msg);
            }
        }
    }

    private void bindDateViewHolder(DateViewHolder holder, ChatItem item) {
        holder.dateText.setText(item.getDateHeader());
    }

    private void bindSenderViewHolder(SenderViewHolder holder, MessageModel msg) {
        holder.senderMsg.setText(msg.getMessage());
        holder.senderTime.setText(formatTime(msg.getTimestamp()));

        Log.d(TAG, "Message status: " + msg.isSeen());
        Log.d(TAG, "Message status: " + msg.getMessage());

        // Update message status
        if (msg.isSeen()) {
            holder.messageStatus.setImageResource(R.drawable.double_tick);
            holder.messageStatus.setColorFilter(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.blue),
                    PorterDuff.Mode.SRC_IN
            );
        } else {
            holder.messageStatus.setImageResource(R.drawable.signle_tick);
            holder.messageStatus.setColorFilter(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.gray),
                    PorterDuff.Mode.SRC_IN
            );
        }
    }

    private void bindReceiverViewHolder(ReceiverViewHolder holder, MessageModel msg) {
        holder.receiverMsg.setText(msg.getMessage());
        holder.receiverTime.setText(formatTime(msg.getTimestamp()));
    }

    private void setupMessageLongClickListener(RecyclerView.ViewHolder holder, MessageModel msg, int position, Context context) {
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(msg, position, context);
            return true;
        });
    }

    private void showDeleteDialog(MessageModel msg, int position, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMessage(msg, position, context))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteMessage(MessageModel msg, int position, Context context) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated while trying to delete message");
            showError("Authentication error", context);
            return;
        }

        String userId = currentUser.getUid();
        String senderRoom = userId + recId;
        String receiverRoom = recId + userId;

        // Delete from sender's chat
        databaseReference.child("chats")
                .child(userId)
                .child(senderRoom)
                .child(msg.getMessageId())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Message deleted successfully");
                    updateLastMessage(userId, senderRoom, receiverRoom);

                    // Notify listener
                    if (deleteListener != null) {
                        deleteListener.onMessageDeleted(msg.getMessageId());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete message", e);
                    showError("Failed to delete message", context);

                    if (deleteListener != null) {
                        deleteListener.onDeleteError(e.getMessage());
                    }
                });
    }

    private void updateLastMessage(String userId, String senderRoom, String receiverRoom) {
        // Get the last message from sender's chat
        databaseReference.child("chats")
                .child(userId)
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
                        updateChatList(userId, recId, lastMessage);

                        // Update ChatList for receiver
                        updateReceiverLastMessage(userId, receiverRoom);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to get last message", error.toException());
                    }
                });
    }

//    private void updateChatList(String userId, String chatId, String lastMessage) {
//        databaseReference.child("ChatList")
//                .child(userId)
//                .child(chatId)
//                .child("lastMessage")
//                .setValue(lastMessage)
//                .addOnFailureListener(e ->
//                        Log.e(TAG, "Failed to update ChatList", e)
//                );
//    }

    private void updateChatList(String userId, String chatPartnerId, String lastMessage) {
        if (userId == null || chatPartnerId == null) {
            Log.e(TAG, "updateChatList failed: userId or chatPartnerId is null. userId=" + userId + ", chatId=" + chatPartnerId);
            return;
        }

        databaseReference.child("ChatList")
                .child(userId)
                .child(chatPartnerId)
                .child("lastMessage")
                .setValue(lastMessage)
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to update ChatList", e)
                );
    }

//    private void updateReceiverLastMessage(String userId, String receiverRoom) {
//        databaseReference.child("chats")
//                .child(recId)
//                .child(receiverRoom)
//                .limitToLast(1)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        String lastMessage = "";
//
//                        for (DataSnapshot messageSnap : snapshot.getChildren()) {
//                            String message = messageSnap.child("message").getValue(String.class);
//                            if (message != null) {
//                                lastMessage = message;
//                            }
//                        }
//
//                        // Update ChatList for receiver
//                        updateChatList(recId, userId, lastMessage);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Log.e(TAG, "Failed to get receiver's last message", error.toException());
//                    }
//                });
//    }

    private void updateReceiverLastMessage(String userId, String receiverRoom) {
        if (userId == null || receiverRoom == null || recId == null) {
            Log.e(TAG, "updateReceiverLastMessage: userId, recId, or receiverRoom is null");
            return;
        }

        databaseReference.child("chats")
                .child(recId)
                .child(receiverRoom)
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

                        // Again, double-check here
                        if (recId != null && userId != null) {
                            updateChatList(recId, userId, lastMessage);
                        } else {
                            Log.e(TAG, "Null userId or recId when updating receiver's ChatList");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to get receiver's last message", error.toException());
                    }
                });
    }


    private void showError(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    private String formatTime(long timestamp) {
        return timeFormatter.format(new Date(timestamp));
    }

    // ViewHolder classes
    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        DateViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiverMsg, receiverTime;

        ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.recieverText);
            receiverTime = itemView.findViewById(R.id.recieverTime);
        }
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg, senderTime;
        ImageView messageStatus;

        SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            messageStatus = itemView.findViewById(R.id.messageStatus);
        }
    }
}