package com.augmentis.ayp.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class PollStarterReceiver extends BroadcastReceiver {
    private static final String TAG = "PollStarterReceiver";

    public PollStarterReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"PollStarterReceiver is start");

        Boolean isOn = PhotoGalleryPreferance.getStoredIsAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);

        Log.d(TAG, "Status of Service Alarm is: " + isOn);
    }
}
