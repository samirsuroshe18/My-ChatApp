//package com.example.mychatapp.Adapters;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.mychatapp.ChatDetailActivity;
//import com.example.mychatapp.Models.ChatlistModel;
//import com.example.mychatapp.R;
//import com.example.mychatapp.utils.DateFormatter;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.FirebaseDatabase;
//import com.squareup.picasso.Picasso;
//
//import java.util.ArrayList;
//import java.util.Date;
//
//public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>{
//    public static final String TAG = "UsersAdapter";
//    ArrayList<ChatlistModel> list;
//    Context context;
//    FirebaseAuth auth = FirebaseAuth.getInstance();
//
//    public UsersAdapter(ArrayList<ChatlistModel> list, Context context) {
//        this.list = list;
//        this.context = context;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        ChatlistModel users = list.get(position);
//
//        Picasso.get().load(users.getProfilepic()).placeholder(R.drawable.profile_pic_avatar).into(holder.image);
//        holder.userName.setText(users.getUserName());
//        holder.lastMessage.setText(users.getLastMessage());
//        holder.timestampText.setText(DateFormatter.formatMessageTime(new Date(users.getLastMsgTime())));
//
//        Log.d(TAG, "onBindViewHolder: If condition "+users.getLastMessageBy().equals(auth.getUid()));
//        Log.d(TAG, "onBindViewHolder: "+auth.getUid());
//        Log.d(TAG, "onBindViewHolder: "+users.getLastMessageBy());
//        Log.d(TAG, "onBindViewHolder: "+users.getReadCount());
//
//        if(users.getLastMessageBy().equals(auth.getUid())){
//            if (users.getReadCount() > 0) {
//                holder.messageStatusIcon.setVisibility(View.GONE);
//            }else if (users.isRead()) {
//                Log.d(TAG, "onBindViewHolder: It's been read");
//                holder.messageStatusIcon.setVisibility(View.VISIBLE);
//                holder.messageStatusIcon.setImageResource(R.drawable.task_alt);
//            }else {
//                Log.d(TAG, "onBindViewHolder: It's been not read");
//                holder.messageStatusIcon.setVisibility(View.VISIBLE);
//                holder.messageStatusIcon.setImageResource(R.drawable.check);
//            }
//        }else{
//            if(!users.isRead() && users.getReadCount() > 0){
//                holder.unreadBadge.setVisibility(View.VISIBLE);
//                holder.unreadBadge.setText(String.valueOf(users.getReadCount()));
//            }
//        }
//
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, ChatDetailActivity.class);
//                intent.putExtra("userId", users.getUserId().toString());
//                intent.putExtra("profilePic", users.getProfilepic());
//                intent.putExtra("userName", users.getUserName());
//                context.startActivity(intent);
//            }
//        });
//
//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                new AlertDialog.Builder(context)
//                        .setTitle("Delete")
//                        .setMessage("Are you sure you want to delete this message")
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                String userId = FirebaseAuth.getInstance().getUid();
//                                String recId = users.getUserId();
//                                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                                String senderRoom = userId + recId;
//                                database.getReference().child("ChatList").child(userId).child(recId).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
//                                        String senderRoom = userId + recId;
//                                        database.getReference().child("chats").child(userId).child(senderRoom).setValue(null);
//                                    }
//                                });
//                            }
//                        })
//                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .show();
//                return true;
//            }
//        });
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    static class ViewHolder extends RecyclerView.ViewHolder{
//        ImageView image, messageStatusIcon;
//        TextView userName,lastMessage, timestampText, unreadBadge;
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            image = itemView.findViewById(R.id.profileImage);
//            messageStatusIcon = itemView.findViewById(R.id.messageStatusIcon);
//            userName = itemView.findViewById(R.id.userName);
//            lastMessage = itemView.findViewById(R.id.aboutMessage);
//            timestampText = itemView.findViewById(R.id.timestampText);
//            unreadBadge = itemView.findViewById(R.id.unreadBadge);
//        }
//    }
//}


package com.example.mychatapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>{
    public static final String TAG = "UsersAdapter";
    ArrayList<ChatlistModel> list;
    Context context;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    public UsersAdapter(ArrayList<ChatlistModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatlistModel users = list.get(position);

        Picasso.get().load(users.getProfilepic()).placeholder(R.drawable.profile_pic_avatar).into(holder.image);
        holder.userName.setText(users.getUserName());
        holder.lastMessage.setText(users.getLastMessage());
        holder.timestampText.setText(DateFormatter.formatMessageTime(new Date(users.getLastMsgTime())));

        // Reset views to default state
        holder.messageStatusIcon.setVisibility(View.GONE);
        holder.unreadBadge.setVisibility(View.GONE);

        Log.d(TAG, "=== onBindViewHolder Debug ===");
        Log.d(TAG, "Position: " + position);
        Log.d(TAG, "UserName: " + users.getUserName());
        Log.d(TAG, "isTyping: " + users.isTyping());
        Log.d(TAG, "==============================");

        // Handle typing indicator
        if (users.isTyping()) {
            Log.d(TAG, "User " + users.getUserName() + " is typing - showing indicator");
            holder.lastMessage.setVisibility(View.GONE);
            holder.typingIndicator.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "User " + users.getUserName() + " is NOT typing - hiding indicator");
            holder.lastMessage.setVisibility(View.VISIBLE);
            holder.typingIndicator.setVisibility(View.GONE);
        }

        if(users.getLastMessageBy() != null && users.getLastMessageBy().equals(auth.getUid())){
            // Current user sent the last message - show message status
            Log.d(TAG, "onBindViewHolder: isReadByUser: "+users.isReadByUser());
            if (users.isReadByUser()) {
                Log.d(TAG, "onBindViewHolder: Message has been read - showing double check");
                holder.messageStatusIcon.setVisibility(View.VISIBLE);
                holder.messageStatusIcon.setImageResource(R.drawable.double_tick);
                holder.messageStatusIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.blue), PorterDuff.Mode.SRC_IN);
            } else {
                Log.d(TAG, "onBindViewHolder: Message not read yet - showing single check");
                holder.messageStatusIcon.setVisibility(View.VISIBLE);
                holder.messageStatusIcon.setImageResource(R.drawable.signle_tick);
                holder.messageStatusIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray), PorterDuff.Mode.SRC_IN);
            }
        } else {
            // Other user sent the last message - show unread badge if applicable
            if(users.getReadCount() > 0) {
                Log.d(TAG, "onBindViewHolder: Showing unread badge with count: " + users.getReadCount());
                holder.unreadBadge.setVisibility(View.VISIBLE);
                holder.unreadBadge.setText(String.valueOf(users.getReadCount()));
            } else {
                Log.d(TAG, "onBindViewHolder: No unread messages - hiding badge");
                holder.unreadBadge.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userId", users.getUserId().toString());
                intent.putExtra("profilePic", users.getProfilepic());
                intent.putExtra("userName", users.getUserName());
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete this conversation?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String userId = FirebaseAuth.getInstance().getUid();
                                String recId = users.getUserId();
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String senderRoom = userId + recId;

                                // Delete from ChatList first
                                database.getReference().child("ChatList").child(userId).child(recId).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // Then delete the chat messages
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        String senderRoom = userId + recId;
                                        database.getReference().child("chats").child(userId).child(senderRoom).setValue(null);
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image, messageStatusIcon;
        TextView userName,lastMessage, timestampText, unreadBadge, typingIndicator;
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