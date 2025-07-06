package com.example.mychatapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mychatapp.ChatDetailActivity;
import com.example.mychatapp.Models.ChatlistModel;
import com.example.mychatapp.R;
import com.example.mychatapp.utils.DateFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private static final String TAG = "UsersAdapter";

    // Firebase references - reused to avoid multiple instance creation
    private final FirebaseAuth auth;
    private final DatabaseReference databaseRef;
    private final String currentUserId;

    private final ArrayList<ChatlistModel> chatList;

    // Interface for handling adapter interactions
    public interface OnChatInteractionListener {
        void onChatClicked(ChatlistModel chat);
        void onChatDeleted(ChatlistModel chat);
    }

    private OnChatInteractionListener interactionListener;

    public UsersAdapter(ArrayList<ChatlistModel> chatList) {
        this.chatList = chatList;
        this.auth = FirebaseAuth.getInstance();
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
        this.currentUserId = auth.getUid();
    }

    public void setOnChatInteractionListener(OnChatInteractionListener listener) {
        this.interactionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_show_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatlistModel chat = chatList.get(position);

        // Load profile image with better error handling
        loadProfileImage(holder.image, chat.getProfilepic());

        // Set basic info
        holder.userName.setText(chat.getUserName());
        holder.lastMessage.setText(chat.getLastMessage());
        holder.timestampText.setText(DateFormatter.formatMessageTime(new Date(chat.getLastMsgTime())));

        // Reset views to default state
        resetViewStates(holder);

        // Handle typing indicator
        handleTypingIndicator(holder, chat);

        // Handle message status and unread count
        handleMessageStatus(holder, chat);

        // Set click listeners
        setClickListeners(holder, chat, position, holder.itemView.getContext());
    }

    private void loadProfileImage(ImageView imageView, String profilePicUrl) {
        Picasso.get()
                .load(profilePicUrl)
                .placeholder(R.drawable.profile_pic_avatar)
                .error(R.drawable.profile_pic_avatar)
                .fit()
                .centerCrop()
                .into(imageView);
    }

    private void resetViewStates(ViewHolder holder) {
        holder.messageStatusIcon.setVisibility(View.GONE);
        holder.unreadBadge.setVisibility(View.GONE);
        holder.typingIndicator.setVisibility(View.GONE);
        holder.lastMessage.setVisibility(View.VISIBLE);
    }

    private void handleTypingIndicator(ViewHolder holder, ChatlistModel chat) {
        if (chat.isTyping()) {
            Log.d(TAG, "User " + chat.getUserName() + " is typing");
            holder.lastMessage.setVisibility(View.GONE);
            holder.typingIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.lastMessage.setVisibility(View.VISIBLE);
            holder.typingIndicator.setVisibility(View.GONE);
        }
    }

    private void handleMessageStatus(ViewHolder holder, ChatlistModel chat) {
        String lastMessageBy = chat.getLastMessageBy();

        if (lastMessageBy != null && lastMessageBy.equals(currentUserId)) {
            // Current user sent the last message - show message status
            showMessageStatus(holder, chat);
        } else {
            // Other user sent the last message - show unread badge if applicable
            showUnreadBadge(holder, chat);
        }
    }

    private void showMessageStatus(ViewHolder holder, ChatlistModel chat) {
        holder.messageStatusIcon.setVisibility(View.VISIBLE);

        if (chat.isReadByUser()) {
            holder.messageStatusIcon.setImageResource(R.drawable.double_tick);
            holder.messageStatusIcon.setColorFilter(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.blue),
                    PorterDuff.Mode.SRC_IN
            );
        } else {
            holder.messageStatusIcon.setImageResource(R.drawable.signle_tick);
            holder.messageStatusIcon.setColorFilter(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.gray),
                    PorterDuff.Mode.SRC_IN
            );
        }
    }

    private void showUnreadBadge(ViewHolder holder, ChatlistModel chat) {
        long unreadCount = chat.getReadCount();
        if (unreadCount > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText(String.valueOf(unreadCount));
        }
    }

    private void setClickListeners(ViewHolder holder, ChatlistModel chat, int position, Context context) {
        // Regular click listener
        holder.itemView.setOnClickListener(v -> {
            if (interactionListener != null) {
                interactionListener.onChatClicked(chat);
            } else {
                // Fallback to direct intent
                openChatActivity(chat, context);
            }
        });

        // Long click listener for delete
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(chat, position, holder.itemView.getContext());
            return true;
        });
    }

    private void openChatActivity(ChatlistModel chat, Context context) {
        Intent intent = new Intent(context, ChatDetailActivity.class);
        intent.putExtra("userId", chat.getUserId());
        intent.putExtra("profilePic", chat.getProfilepic());
        intent.putExtra("userName", chat.getUserName());
        context.startActivity(intent);
    }

    private void showDeleteDialog(ChatlistModel chat, int position, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Conversation")
                .setMessage("Are you sure you want to delete this conversation? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteChatConversation(chat, position))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteChatConversation(ChatlistModel chat, int position) {
        if (currentUserId == null) {
            Log.e(TAG, "Current user ID is null");
            return;
        }

        String receiverId = chat.getUserId();
        String senderRoom = currentUserId + receiverId;

        // Only delete current user's chat list and messages
        Map<String, Object> updates = new HashMap<>();
        updates.put("ChatList/" + currentUserId + "/" + receiverId, null); // Delete from current user's chat list
        updates.put("chats/" + currentUserId + "/" + senderRoom, null);    // Delete messages from current user's node

        databaseRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat conversation deleted for current user");

                    // Remove from UI
                    if (position >= 0 && position < chatList.size()) {
                        chatList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, chatList.size());
                    } else {
                        Log.w(TAG, "Tried to remove invalid index: " + position);
                    }

                    if (interactionListener != null) {
                        interactionListener.onChatDeleted(chat);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete chat conversation", e);
                    // Optionally show a toast or snackbar to the user
                });
    }

    // Method to update a specific item in the list
    public void updateItem(int position, ChatlistModel updatedChat) {
        if (position >= 0 && position < chatList.size()) {
            chatList.set(position, updatedChat);
            notifyItemChanged(position);
        }
    }

    // Method to add new chat item
    public void addItem(ChatlistModel newChat) {
        chatList.add(0, newChat); // Add at the beginning
        notifyItemInserted(0);
    }

    // Method to remove item
    public void removeItem(int position) {
        if (position >= 0 && position < chatList.size()) {
            chatList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, chatList.size());
        }
    }

    // Method to clear all items
    public void clearItems() {
        int size = chatList.size();
        chatList.clear();
        notifyItemRangeRemoved(0, size);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final ImageView messageStatusIcon;
        final TextView userName;
        final TextView lastMessage;
        final TextView timestampText;
        final TextView unreadBadge;
        final TextView typingIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profileImage);
            messageStatusIcon = itemView.findViewById(R.id.messageStatusIcon);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.aboutMessage);
            timestampText = itemView.findViewById(R.id.timestampText);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
            typingIndicator = itemView.findViewById(R.id.typingIndicator);
        }
    }
}