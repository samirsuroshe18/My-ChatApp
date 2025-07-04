package com.example.mychatapp.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mychatapp.ChatDetailActivity;
import com.example.mychatapp.Models.Users;
import com.example.mychatapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectContctAdapter extends RecyclerView.Adapter<SelectContctAdapter.ViewHolder>{
    ArrayList<Users> userList;

    public SelectContctAdapter(ArrayList<Users> list) {
        this.userList = list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView userName;
        TextView aboutMessage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            aboutMessage = itemView.findViewById(R.id.aboutMessage);
        }
    }

    @NonNull
    @Override
    public SelectContctAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_select_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectContctAdapter.ViewHolder holder, int position) {
        Users users = userList.get(position);

        Picasso.get().load(users.getProfilepic()).placeholder(R.drawable.profile_pic_avatar).into(holder.profileImage);
        holder.userName.setText(users.getUserName());
        holder.aboutMessage.setText(users.getAbout());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), ChatDetailActivity.class);
                intent.putExtra("userId", users.getUserId().toString());
                intent.putExtra("profilePic", users.getProfilepic());
                intent.putExtra("userName", users.getUserName());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
