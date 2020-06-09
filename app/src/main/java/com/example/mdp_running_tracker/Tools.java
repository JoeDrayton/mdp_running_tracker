package com.example.mdp_running_tracker;

import android.content.Context;
import android.widget.Toast;

/**
 * Quick tool class to display an exception toast to the screen
 */
public class Tools  {
    public static void exceptionToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}