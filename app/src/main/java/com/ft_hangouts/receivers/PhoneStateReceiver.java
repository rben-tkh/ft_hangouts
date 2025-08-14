package com.ft_hangouts.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.ft_hangouts.activities.MainActivity;

public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = "PhoneStateReceiver";
    private static boolean callInProgress = false;
    private static String calledFromApp = null;

    public static void setCallInProgress(boolean inProgress, String appPackage) {
        callInProgress = inProgress;
        calledFromApp = appPackage;
        Log.d(TAG, "Call in progress: " + inProgress + ", from app: " + appPackage);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(TAG, "Phone state changed: " + state);

            if (TelephonyManager.EXTRA_STATE_IDLE.equals(state) && callInProgress && calledFromApp != null) {
                Log.d(TAG, "Call ended, returning to app: " + calledFromApp);
                
                Intent appIntent = new Intent(context, MainActivity.class);
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                appIntent.putExtra("RETURN_FROM_CALL", true);
                
                try {
                    context.startActivity(appIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Error returning to app: " + e.getMessage());
                }
                
                callInProgress = false;
                calledFromApp = null;
            }
        }
    }
}