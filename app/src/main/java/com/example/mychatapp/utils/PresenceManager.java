package com.example.mychatapp.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

/**
 * Keeps Users/{uid}/status in sync with whether the app is on screen.
 * The value is "online" while visible and a last seen timestamp otherwise.
 */
public final class PresenceManager {
    public static final String STATUS_ONLINE = "online";

    private PresenceManager() {
    }

    public static void goOnline() {
        DatabaseReference statusRef = statusRef();
        if (statusRef == null) {
            return;
        }
        // If the connection drops without a clean exit, the server records the last seen time
        statusRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
        statusRef.setValue(STATUS_ONLINE);
    }

    public static void goOffline() {
        DatabaseReference statusRef = statusRef();
        if (statusRef == null) {
            return;
        }
        statusRef.setValue(ServerValue.TIMESTAMP);
    }

    private static DatabaseReference statusRef() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            return null;
        }
        return FirebaseDatabase.getInstance().getReference("Users").child(userId).child("status");
    }
}
