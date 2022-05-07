package com.reichman.messaging;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // On New Token
    @Override
    public void onNewToken(@NonNull String token) {
        // Instantiate Super Class
        super.onNewToken(token);

        // Log Token
        Log.e("NewToken", token);

        // Save In Shared Preferences
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fcm_token", token).apply();
    }

    // On Notification Message Received
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        // Instantiate Super Class
        super.onMessageReceived(remoteMessage);
    }

    // Get Token From Shared Preferences
    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fcm_token", "No Token");
    }
}
