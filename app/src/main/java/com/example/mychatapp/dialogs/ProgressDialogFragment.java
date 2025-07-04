package com.example.mychatapp.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mychatapp.R;

/**
 * Beautiful, modern progress dialog with smooth animations and Material Design 3 styling.
 * Features include:
 * - Elegant card-based design with rounded corners and elevation
 * - Smooth fade-in/fade-out animations
 * - Optional animated dots indicator
 * - Customizable messages and subtitles
 * - Proper lifecycle management
 */
public class ProgressDialogFragment extends DialogFragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBTITLE = "subtitle";

    public static ProgressDialogFragment newInstance(String title, String subtitle) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_SUBTITLE, subtitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_progress, container, false);

        // Find your TextViews
        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView subtitleView = view.findViewById(R.id.progressMessage);

        // Get arguments and set text
        Bundle args = getArguments();
        if (args != null) {
            titleView.setText(args.getString(ARG_TITLE, ""));
            subtitleView.setText(args.getString(ARG_SUBTITLE, ""));
        }

        return view;
    }
}
