package com.example.mychatapp.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.mychatapp.Models.Users;
import com.example.mychatapp.R;
import com.example.mychatapp.SignInActivity;
import com.example.mychatapp.databinding.FragmentSettingBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class UserProfileFragment extends Fragment {

    private FragmentSettingBinding binding;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private Uri selectedImageUri;

    // Activity Result launcher for image picker
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Hide action bar if you want (optional, depends on your host activity)
        // requireActivity().getSupportActionBar().hide();

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            selectedImageUri = uri;
                            binding.profileImg.setImageURI(uri);
                            uploadProfileImage(uri);
                        }
                    }
                }
        );

        // Save button click
        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = binding.etStatus.getText().toString();
                String userName = binding.etUsername.getText().toString();

                HashMap<String, Object> obj = new HashMap<>();
                obj.put("userName", userName);
                obj.put("about", status);

                database.getReference().child("Users").child(auth.getUid()).updateChildren(obj);
                Toast.makeText(getContext(), "Changes Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        binding.logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setCancelable(true)
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Show loading
                                ProgressDialog progressDialog = new ProgressDialog(requireContext());
                                progressDialog.setMessage("Logging out...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();

                                // Optional delay for smoother UX
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    progressDialog.dismiss();
                                    String userId = FirebaseAuth.getInstance().getUid();
                                    database.getReference().child("Users").child(userId).child("FCMToken").removeValue();
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent(requireContext(), SignInActivity.class);
                                    startActivity(intent);
                                    requireActivity().finishAffinity();
                                }, 1000);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        // Load user info
        database.getReference().child("Users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = snapshot.getValue(Users.class);
                        if (users != null) {
                            Picasso.get().load(users.getProfilepic())
                                    .placeholder(R.drawable.profile_pic_avatar).into(binding.profileImg);

                            binding.etStatus.setText(users.getAbout());
                            binding.etUsername.setText(users.getUserName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Add button click (pick image)
        binding.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickerLauncher.launch("image/*");
            }
        });
    }

    // Upload profile image to Firebase Storage
    private void uploadProfileImage(Uri fileUri) {
        final StorageReference reference = storage.getReference().child("profilepic")
                .child(auth.getUid());

        reference.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        database.getReference().child("Users").child(auth.getUid())
                                .child("profilepic").setValue(uri.toString());
                        Toast.makeText(getContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Optional: Handle toolbar menu if needed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
