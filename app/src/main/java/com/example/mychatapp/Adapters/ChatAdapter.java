package com.example.mychatapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.example.mychatapp.Models.ChatItem;
import com.example.mychatapp.Models.MessageModel;
import com.example.mychatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int SENDER_VIEW_TYPE = 1;
    private static final int RECEIVER_VIEW_TYPE = 2;
    private static final int DATE_VIEW_TYPE = 3;
    String recId;
    private final List<ChatItem> chatItems;

    public ChatAdapter(ArrayList<ChatItem> chatItems, String recId) {
        this.recId = recId;
        this.chatItems = chatItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        } else if (viewType == RECEIVER_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_reciever, parent, false);
            return new ReceiverViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new DateViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatItem item = chatItems.get(position);
        if (item.getType() == ChatItem.TYPE_DATE) {
            return DATE_VIEW_TYPE;
        } else {
            MessageModel msg = item.getMessageModel();
            if (msg.getuId().equals(FirebaseAuth.getInstance().getUid())) {
                return SENDER_VIEW_TYPE;
            } else {
                return RECEIVER_VIEW_TYPE;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem item = chatItems.get(position);

        if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).dateText.setText(item.getDateHeader());
        } else {
            MessageModel msg = item.getMessageModel();

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(holder.itemView.getContext())
                            .setTitle("Delete")
                            .setMessage("Are you sure you want to delete this message")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String userId = FirebaseAuth.getInstance().getUid();
                                    if (userId == null) {
                                        // User is not authenticated - shouldn't happen in chat screen
                                        Log.e("ChatAdapter", "User not authenticated while trying to delete message");
                                        dialog.dismiss();
                                        return;
                                    }
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    String senderRoom = userId + recId;
                                    String receiverRoom = recId + userId;
                                    database.getReference().child("chats").child(userId).child(senderRoom).child(msg.getMessageId()).setValue(null)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    // ✅ Step 2: Get the last message remaining
                                                    database.getReference().child("chats")
                                                            .child(userId)
                                                            .child(senderRoom)
                                                            .limitToLast(1)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    String lastMessage = "";
//                                                                    long timestamp = 0;

                                                                    for (DataSnapshot messageSnap : snapshot.getChildren()) {
                                                                        lastMessage = messageSnap.child("message").getValue(String.class);
//                                                                        Long ts = messageSnap.child("timestamp").getValue(Long.class);
//                                                                        timestamp = ts != null ? ts : 0;
                                                                    }

                                                                    // ✅ Step 3: Update ChatList
                                                                    database.getReference().child("ChatList")
                                                                            .child(userId)
                                                                            .child(recId)
                                                                            .child("lastMessage")
                                                                            .setValue(lastMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    database.getReference().child("chats")
                                                                                            .child(recId)
                                                                                            .child(receiverRoom)
                                                                                            .limitToLast(1)
                                                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                @Override
                                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                                    String lastMessage = "";
                                                                                                    // long timestamp = 0;

                                                                                                    for (DataSnapshot messageSnap : snapshot.getChildren()) {
                                                                                                        lastMessage = messageSnap.child("message").getValue(String.class);
                                                                                                        // Long ts = messageSnap.child("timestamp").getValue(Long.class);
                                                                                                        // timestamp = ts != null ? ts : 0;
                                                                                                    }

                                                                                                    database.getReference().child("ChatList")
                                                                                                            .child(recId)
                                                                                                            .child(userId)
                                                                                                            .child("lastMessage")
                                                                                                            .setValue(lastMessage);
                                                                                                }

                                                                                                @Override
                                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                                }
                                                                                            });
                                                                                }
                                                                            });
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
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

            if (holder instanceof SenderViewHolder) {
                ((SenderViewHolder) holder).senderMsg.setText(msg.getMessage());
                ((SenderViewHolder) holder).senderTime.setText(formatTime(msg.getTimestamp()));
                if (msg.isSeen()) {
                    ((SenderViewHolder) holder).messageStatus.setImageResource(R.drawable.double_tick);
                    ((SenderViewHolder) holder).messageStatus.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.blue), PorterDuff.Mode.SRC_IN);
                } else {
                    ((SenderViewHolder) holder).messageStatus.setImageResource(R.drawable.signle_tick);
                    ((SenderViewHolder) holder).messageStatus.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray), PorterDuff.Mode.SRC_IN);
                }

            } else if (holder instanceof ReceiverViewHolder) {
                ((ReceiverViewHolder) holder).receiverMsg.setText(msg.getMessage());
                ((ReceiverViewHolder) holder).receiverTime.setText(formatTime(msg.getTimestamp()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    private String formatTime(long timestamp) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(timestamp));
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        DateViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiverMsg, receiverTime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverMsg = itemView.findViewById(R.id.recieverText);
            receiverTime = itemView.findViewById(R.id.recieverTime);
        }
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg, senderTime;
        ImageView messageStatus;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            messageStatus = itemView.findViewById(R.id.messageStatus);
        }
    }
}
