package com.example.mychatapp.Fragments;

import android.app.Activity;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
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
                String status = binding.etStatus.getText().toString().trim();
                String userName = binding.etUsername.getText().toString().trim();

                if (userName.isEmpty()) {
                    binding.etUsername.setError("Username is required");
                    binding.etUsername.requestFocus();
                    return;
                }

                HashMap<String, Object> obj = new HashMap<>();
                obj.put("userName", userName);
                obj.put("about", status);

                binding.etStatus.clearFocus();
                binding.etUsername.clearFocus();
                database.getReference().child("Users").child(auth.getUid()).updateChildren(obj)
                        .addOnCompleteListener(task -> {
                            if (getContext() == null) return;
                            String message = task.isSuccessful() ? "Changes Updated Successfully" : "Failed to update. Please try again";
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        });
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
                                logout();
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
                        if (users != null && binding != null) {
                            String profilePic = users.getProfilepic();
                            if (profilePic != null && !profilePic.trim().isEmpty()) {
                                Picasso.get().load(profilePic)
                                        .placeholder(R.drawable.profile_pic_avatar).into(binding.profileImg);
                            }

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
    private void logout() {
        Activity activity = requireActivity();
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Logging out...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final boolean[] finished = {false};
        Runnable finishLogout = () -> {
            if (finished[0]) return;
            finished[0] = true;

            // Without this the next Google sign-in silently reuses the same account
            GoogleSignIn.getClient(activity.getApplicationContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
            FirebaseAuth.getInstance().signOut();

            if (activity.isFinishing() || activity.isDestroyed()) return;
            progressDialog.dismiss();
            activity.startActivity(new Intent(activity, SignInActivity.class));
            activity.finishAffinity();
        };

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            finishLogout.run();
            return;
        }

        DatabaseReference userRef = database.getReference().child("Users").child(userId);
        userRef.child("status").onDisconnect().cancel();

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("FCMToken", null);
        updates.put("status", ServerValue.TIMESTAMP);

        // These writes need the signed in session, so sign out only once they are done
        userRef.updateChildren(updates).addOnCompleteListener(task -> finishLogout.run());
        // Do not keep the user waiting when the device is offline
        new Handler(Looper.getMainLooper()).postDelayed(finishLogout, 4000);
    }

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
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        }
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
