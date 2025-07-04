package com.example.mychatapp.utils;

import androidx.recyclerview.widget.DiffUtil;

import com.example.mychatapp.Models.ChatItem;
import com.example.mychatapp.Models.MessageModel;

import java.util.List;

public class ChatItemDiffCallback extends DiffUtil.Callback {
    private final List<ChatItem> oldList;
    private final List<ChatItem> newList;

    public ChatItemDiffCallback(List<ChatItem> oldList, List<ChatItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        ChatItem oldItem = oldList.get(oldItemPosition);
        ChatItem newItem = newList.get(newItemPosition);

        // Compare based on your ChatItem structure
        if (oldItem.getType() == ChatItem.TYPE_DATE && newItem.getType() == ChatItem.TYPE_DATE) {
            return oldItem.getDateHeader().equals(newItem.getDateHeader());
        } else if (oldItem.getType() == ChatItem.TYPE_MESSAGE && newItem.getType() == ChatItem.TYPE_MESSAGE) {
            return oldItem.getMessageModel().getMessageId().equals(newItem.getMessageModel().getMessageId());
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ChatItem oldItem = oldList.get(oldItemPosition);
        ChatItem newItem = newList.get(newItemPosition);

        if (oldItem.getType() == ChatItem.TYPE_DATE && newItem.getType() == ChatItem.TYPE_DATE) {
            return oldItem.getDateHeader().equals(newItem.getDateHeader());
        } else if (oldItem.getType() == ChatItem.TYPE_MESSAGE && newItem.getType() == ChatItem.TYPE_MESSAGE) {
            MessageModel oldMsg = oldItem.getMessageModel();
            MessageModel newMsg = newItem.getMessageModel();
            return oldMsg.equals(newMsg); // Make sure MessageModel has proper equals() method
        }
        return false;
    }
}