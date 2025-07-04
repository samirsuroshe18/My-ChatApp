package com.example.mychatapp.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

public class AlertDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle("Alert")
                .setMessage("Are you sure you want to proceed?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Handle positive action
                })
                .setNegativeButton("No", null)
                .create();
    }

    public static final String TAG = "AlertDialog";
}