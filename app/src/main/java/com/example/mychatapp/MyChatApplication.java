package com.example.mychatapp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mychatapp.utils.PresenceManager;

public class MyChatApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private int startedActivities = 0;
    private boolean changingConfiguration = false;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        startedActivities++;
        // A rotation restarts the activity, but the app never left the screen
        if (startedActivities == 1 && !changingConfiguration) {
            PresenceManager.goOnline();
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        changingConfiguration = activity.isChangingConfigurations();
        startedActivities--;
        if (startedActivities == 0 && !changingConfiguration) {
            PresenceManager.goOffline();
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
