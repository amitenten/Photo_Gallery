package com.augmentis.ayp.photogallery;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amita on 8/23/2016.
 */
@TargetApi(21)
public class PollJobService extends JobService{

    private static final int JOB_ID = 122;
    private static final String TAG = "PollJobService";
    private PollTask mPollTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        mPollTask = new PollTask();
        mPollTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mPollTask != null) {
            mPollTask.cancel(true);
        }
        return false;
    }

    public static boolean isRun(Context context) {
        JobScheduler sch = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        List<JobInfo> jobInfoList = sch.getAllPendingJobs();

        for (JobInfo jobInfo : jobInfoList) {
            if (jobInfo.getId() == JOB_ID) {
                return true;
            }
        }
        return false;
    }

    public static void start(Context context) {

        Log.d(TAG,"Start : Job poll running");

        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollJobService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        builder.setPeriodic(1000*1);
        //builder.setPersisted(true);
        JobInfo jobInfo = builder.build();

        JobScheduler sch = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        sch.schedule(jobInfo);
    }

    public static void stop(Context context) {
        JobScheduler sch = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        sch.cancel(JOB_ID);
    }

    public class PollTask extends AsyncTask<JobParameters, Void, Void> {

        @Override
        protected Void doInBackground(JobParameters... params) {
            //Do job whatever
            String query = PhotoGalleryPreferance.getStoredSearchKey(PollJobService.this);
            String storedLastId = PhotoGalleryPreferance.getStoredLastId(PollJobService.this);

            List<GalleryItem> galleryItemList = new ArrayList<>();

            FlickrFetcher flickrFetcher = new FlickrFetcher();
            if (query == null) {
                flickrFetcher.getRecentPhotos(galleryItemList);
            } else {
                flickrFetcher.searchPhotos(galleryItemList, query);
            }

            if (galleryItemList.size() == 0) {
                return null;
            }

            Log.i(TAG, "Found search or recent photo");

            String newestId = galleryItemList.get(0).getId(); //fetching first item

            if (newestId.equals(storedLastId)) {
                Log.i(TAG, "No new item");
            } else {
                Log.i(TAG, "New item found");

                Resources res = getResources();
                Intent i = PhotoGalleryActivity.newIntent(PollJobService.this);
                PendingIntent pi = PendingIntent.getActivity(PollJobService.this, 0, i, 0);

                // to build notification object
                NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(PollJobService.this);
                notiBuilder.setTicker(res.getString(R.string.new_picture_arriving));
                notiBuilder.setSmallIcon(android.R.drawable.ic_menu_report_image);
                notiBuilder.setContentTitle(res.getString(R.string.new_picture_title));
                notiBuilder.setContentText(res.getString(R.string.new_picture_text));
                notiBuilder.setContentIntent(pi);
                notiBuilder.setAutoCancel(true);

                // build noti from builder
                Notification notification = notiBuilder.build();
                NotificationManagerCompat nm = NotificationManagerCompat.from(PollJobService.this);
                // call noti
                nm.notify(0,notification);

                new Screen().on(PollJobService.this);
            }

            PhotoGalleryPreferance.setStoredLastId(PollJobService.this, newestId);
            Log.d(TAG,"Job poll running");
            // finish
            jobFinished(params[0], false);
            return null;
        }
    }
}
