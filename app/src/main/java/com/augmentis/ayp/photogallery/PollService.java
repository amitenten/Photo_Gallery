package com.augmentis.ayp.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amita on 8/22/2016.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 1;

    public static final String ACTION_SHOW_NOTIFICATION = "com.augmentis.ayp.photogallery.ACTION_SHOW_NOTIFICATION";

    public static final String PERMISSION_SHOW_NOTIF = "com.augmentis.ayp.photogallery.RECEIVE_SHOW_NOTIFICATION";
    public static final String REQUEST_CODE = "REQUEST_CODE_INTENT";
    public static final String NOTIFICATION = "NOTIFICATION";

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Receive call from intent ");
        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        Log.i(TAG, "Active network!!! ");

        String query = PhotoGalleryPreferance.getStoredSearchKey(this);
        String storedLastId = PhotoGalleryPreferance.getStoredLastId(this);

        List<GalleryItem> galleryItemList = new ArrayList<>();

        FlickrFetcher flickrFetcher = new FlickrFetcher();
        if (query == null) {
            flickrFetcher.getRecentPhotos(galleryItemList);
        } else {
            flickrFetcher.searchPhotos(galleryItemList, query);
        }

        if (galleryItemList.size() == 0) {
            return;
        }

        Log.i(TAG, "Found search or recent photo");

        String newestId = galleryItemList.get(0).getId(); //fetching first item

        if (newestId.equals(storedLastId)) {
            Log.i(TAG, "No new item");
        } else {
            Log.i(TAG, "New item found");

            Resources res = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            // to build notification object
            NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this);
            notiBuilder.setTicker(res.getString(R.string.new_picture_arriving));
            notiBuilder.setSmallIcon(android.R.drawable.ic_menu_report_image);
            notiBuilder.setContentTitle(res.getString(R.string.new_picture_title));
            notiBuilder.setContentText(res.getString(R.string.new_picture_text));
            notiBuilder.setContentIntent(pi);
            notiBuilder.setAutoCancel(true);

            // build noti from builder
            Notification notification = notiBuilder.build();
 /*           NotificationManagerCompat nm = NotificationManagerCompat.from(this);
            // call noti
            nm.notify(0, notification);

            /*new Screen().on(this);*/

            sendBackgroundNotification(0, notification);
            /*sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERMISSION_SHOW_NOTIF);*/
        }

        PhotoGalleryPreferance.setStoredLastId(this, newestId);
    }

    private void sendBackgroundNotification(int requestCode,Notification notification) {
        Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(REQUEST_CODE,requestCode);
        intent.putExtra(NOTIFICATION,notification);

        sendOrderedBroadcast(intent, PERMISSION_SHOW_NOTIF,null,null, Activity.RESULT_OK,null,null);
    }

    public static void setServiceAlarm(Context c, Boolean isOn) {
        Intent i = PollService.newIntent(c);
        PendingIntent pi = PendingIntent.getService(c, 0, i, 0);

        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME    //param 1: Mode
                    , SystemClock.elapsedRealtime()                 //param 2: Start
                    , POLL_INTERVAL                                  //param 3: Interval
                    , pi);                                           //param 4: Pending Action(Intent)
        } else {
            am.cancel(pi); //cancel interval call
            pi.cancel(); // cancel pending intent call

            Log.d(TAG, "Run by schedule");
        }

        PhotoGalleryPreferance.setStoredIsAlarmOn(c, isOn);
    }

    public static boolean isServiceAlamOn(Context context) {
            Intent i = PollService.newIntent(context);
            PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);

            return pi != null;
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isActiveNetwork = cm.getActiveNetworkInfo() != null;
        boolean isActiveNetworkConnected = isActiveNetwork && cm.getActiveNetworkInfo().isConnected();

        return isActiveNetworkConnected;
    }
}
