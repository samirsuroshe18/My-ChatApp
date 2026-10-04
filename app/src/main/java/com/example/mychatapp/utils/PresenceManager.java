package com.example.mychatapp.utils;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

/**
 * Keeps Users/{uid}/status in sync with whether the app is on screen.
 * The value is "online" while visible and a last seen timestamp otherwise.
 */
public final class PresenceManager {
    public static final String STATUS_ONLINE = "online";

    private static ValueEventListener connectionListener;

    private PresenceManager() {
    }

    public static void goOnline() {
        DatabaseReference statusRef = statusRef();
        if (statusRef == null) {
            return;
        }
        stopWatchingConnection();
        // Runs now and after every reconnect: a dropped connection leaves a last seen time behind
        connectionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                    return;
                }
                // If the connection drops without a clean exit, the server records the last seen time
                statusRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                statusRef.setValue(STATUS_ONLINE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        connectedRef().addValueEventListener(connectionListener);
    }

    public static void goOffline() {
        DatabaseReference statusRef = statusRef();
        if (statusRef == null) {
            return;
        }
        stopWatchingConnection();
        statusRef.setValue(ServerValue.TIMESTAMP);
    }

    private static void stopWatchingConnection() {
        if (connectionListener != null) {
            connectedRef().removeEventListener(connectionListener);
            connectionListener = null;
        }
    }

    private static DatabaseReference connectedRef() {
        return FirebaseDatabase.getInstance().getReference(".info/connected");
    }

    private static DatabaseReference statusRef() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return null;
        }
        return FirebaseDatabase.getInstance().getReference("Users").child(userId).child("status");
    }
}
