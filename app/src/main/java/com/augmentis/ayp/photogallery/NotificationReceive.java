package com.augmentis.ayp.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class NotificationReceive extends BroadcastReceiver {
    private static final String TAG = "NotificationReceive";

    public NotificationReceive() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notification calling");

        if (getResultCode() != Activity.RESULT_OK){
            return;
        }

        Notification notification = (Notification)
                intent.getParcelableExtra(PollService.NOTIFICATION);

        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE, 0);

        NotificationManagerCompat.from(context).notify(requestCode,notification);

        Log.d(TAG, "Notification new item");
    }
}
