package com.example.mychatapp.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationSender {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    static OkHttpClient client = new OkHttpClient();

    static public void sendNotification(String token, String userId, String userName, String textMessage, String profilePic) {
        try {
            JSONObject json = new JSONObject();
            json.put("token", token);
            json.put("userId", userId);
            json.put("userName", userName);
            json.put("textMessage", textMessage);
            json.put("profilePic", profilePic);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url("https://push-notification-zeta.vercel.app/send-notification")
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

