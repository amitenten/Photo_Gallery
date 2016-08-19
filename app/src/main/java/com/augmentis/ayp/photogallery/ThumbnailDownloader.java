package com.augmentis.ayp.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Amita on 8/18/2016.
 */
public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int DOWLOAD_FILE = 2005;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequetUrlMap = new ConcurrentHashMap<>();

    private Handler mResponseHandler;
    private ThumbnailDownloaderListener<T> mThumbnailDownloaderListener;
    private final LruCache<String, Bitmap> mCache = new LruCache(1024);

    interface ThumbnailDownloaderListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail, String url);
    }

    public void setmThumbnailDownloaderListener(ThumbnailDownloaderListener<T> mThumbnailDownloaderListener) {
        this.mThumbnailDownloaderListener = mThumbnailDownloaderListener;
    }

    public ThumbnailDownloader(Handler mUIHandler) {
        super(TAG);

        mResponseHandler = mUIHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // work in the queue
                if (msg.what == DOWLOAD_FILE) {
                    T target = (T) msg.obj;

                    String url = mRequetUrlMap.get(target);
                    Log.i(TAG, "Got message from queue: pls download this url: "+ url);

                    handleRequestDownload(target, url);
                }

            }
        };
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(DOWLOAD_FILE);
    }

    private void handleRequestDownload(final T target, final String url) {
        try {
            if (url == null) {
                return;
            }

            byte[] bitMapByte = new FlickrFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitMapByte,0,bitMapByte.length);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    String currentUrl = mRequetUrlMap.get(target);
                    if (currentUrl != null && !currentUrl.equals(url)) {
                        return;
                    }

                    // url is ok (the same one)
                    mRequetUrlMap.remove(target);
                    mThumbnailDownloaderListener.onThumbnailDownloaded(target, bitmap, url);
                }
            });

            Log.i(TAG,"Bitmap URL download");
        } catch (IOException ioe) {
            Log.i(TAG,"Error Bitmap URL download");
        }
    }

    public void queueThumbnailDownloader(T target, String url) {
        Log.i(TAG, "Got url : " + url);

        if (url == null) {
            mRequetUrlMap.remove(target);
        } else {
            mRequetUrlMap.put(target, url);

            Message msg = mRequestHandler.obtainMessage(DOWLOAD_FILE, target);
            msg.sendToTarget(); // send to handler
        }
    }
}
