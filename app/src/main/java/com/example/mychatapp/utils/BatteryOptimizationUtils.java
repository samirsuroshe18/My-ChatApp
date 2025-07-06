package com.example.mychatapp.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

public class BatteryOptimizationUtils {
    private static final String TAG = "BatteryOptimizationUtils";

    /**
     * Check if battery optimization is enabled for the app
     * @param context Application context
     * @return true if battery optimization is enabled (bad for notifications)
     */
    public static boolean isBatteryOptimizationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                boolean isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
                Log.d(TAG, "Battery optimization enabled: " + !isIgnoring);
                return !isIgnoring; // If NOT ignoring, then optimization is enabled
            }
        }
        return false; // For older Android versions, assume it's fine
    }

    /**
     * Request battery optimization exemption for the app
     * @param context Application context
     */
    public static void requestBatteryOptimizationExemption(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
                Log.d(TAG, "Opened battery optimization settings");
            } catch (Exception e) {
                Log.e(TAG, "Failed to open battery optimization settings", e);
                // Fallback: Open general battery settings
                openBatterySettings(context);
            }
        }
    }

    /**
     * Open general battery settings as fallback
     * @param context Application context
     */
    private static void openBatterySettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
            context.startActivity(intent);
            Log.d(TAG, "Opened general battery settings");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open battery settings", e);
            // Final fallback: Open app settings
            openAppSettings(context);
        }
    }

    /**
     * Open app settings as final fallback
     * @param context Application context
     */
    private static void openAppSettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
            Log.d(TAG, "Opened app settings");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open app settings", e);
        }
    }

    /**
     * Get manufacturer-specific battery optimization info
     * @return String with manufacturer-specific instructions
     */
    public static String getManufacturerSpecificInstructions() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();

        switch (manufacturer) {
            case "samsung":
                return "Samsung: Go to Settings > Apps > Special Access > Optimize battery usage > Select 'All apps' > Find your app > Disable";
            case "huawei":
                return "Huawei: Go to Settings > Apps > Advanced > Ignore battery optimization > Select your app";
            case "xiaomi":
                return "Xiaomi: Go to Settings > Apps > Manage apps > Your app > Battery saver > No restrictions";
            case "oneplus":
                return "OnePlus: Go to Settings > Apps > Special app access > Battery optimization > Your app > Don't optimize";
            case "oppo":
                return "Oppo: Go to Settings > Apps > App list > Your app > Battery > Background app refresh > Allow";
            case "vivo":
                return "Vivo: Go to Settings > Apps > App list > Your app > Battery > Background app refresh > Allow";
            default:
                return "Go to Settings > Apps > Special Access > Battery optimization > Select your app > Don't optimize";
        }
    }
}