package com.example.mychatapp.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationSender {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final String SERVER_URL = "https://push-notification-zeta.vercel.app";

    static OkHttpClient client = new OkHttpClient();

    static public void sendNotification(String token, String userId, String userName, String textMessage, String profilePic) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        // The server only accepts requests from signed in users, so prove who is sending
        currentUser.getIdToken(false).addOnSuccessListener(result ->
                postNotification(result.getToken(), token, userId, userName, textMessage, profilePic));
    }

    private static void postNotification(String idToken, String token, String userId, String userName, String textMessage, String profilePic) {
        try {
            JSONObject json = new JSONObject();
            json.put("token", token);
            json.put("userId", userId);
            json.put("userName", userName);
            json.put("textMessage", textMessage);
            // Send an empty value instead of dropping the field when the sender has no photo
            json.put("profilePic", profilePic != null ? profilePic : "");

            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(SERVER_URL + "/send-notification")
                    .addHeader("Authorization", "Bearer " + idToken)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d("Success: ", response.body().string());
                    } else {
                        Log.d("Error: ", response.code() + " - " + response.body().string());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

