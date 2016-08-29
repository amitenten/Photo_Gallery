package com.augmentis.ayp.photogallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.LruCache;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wilailux on 8/16/2016.
 */
public class PhotoGalleryFragment extends VisibleFragment{

    private String mSearchKey;

    /**
     * newInstance
     * @return fragment
     */
    public static PhotoGalleryFragment newInstance() {

        Bundle args = new Bundle();
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mRecyclerView;
    private FlickrFetcher mFlickrFetcher;
    //private PhotoGalleryAdapter mAdapter;
    private List<GalleryItem> mItems;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloaderThread;
    private FetcherTask mFetcherTask;
    private LruCache<String, Bitmap> mMemoryCache;
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);
    final int cacheSize = maxMemory/8;

    /**
     * On create
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setHasOptionsMenu(true);
        setRetainInstance(true);

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
        /*new FetcherTask().execute();// run another thread*/

        Handler responseUIHandler = new Handler();
        //Looper.getMainLooper();

        ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder> listener =
                new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail, String url) {
                if (mMemoryCache.get(url) == null) {
                    mMemoryCache.put(url, thumbnail);
                }

                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable);
            }
        };

        mThumbnailDownloaderThread = new ThumbnailDownloader<>(responseUIHandler);
        mThumbnailDownloaderThread.setmThumbnailDownloaderListener(listener);
        mThumbnailDownloaderThread.start();
        mThumbnailDownloaderThread.getLooper();

        Log.i(TAG, "Start background thread");
    }

    /**
     * onDestroy
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        mThumbnailDownloaderThread.quit();
        Log.i(TAG, "Stop background thread");

    }

    /**
     * onDestroyView
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mThumbnailDownloaderThread.clearQueue();
    }

    /**
     * onPause
     */
    @Override
    public void onPause() {
        super.onPause();


        PhotoGalleryPreferance.setStoredSearchKey(getActivity(), mSearchKey);
    }

    /**
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();
        String searchKey = PhotoGalleryPreferance.getStoredSearchKey(getActivity());

        if(searchKey != null){
            mSearchKey = searchKey;
        }
    }

    /**
     * onCreateView
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.photo_gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        mItems = new ArrayList<>();
        mRecyclerView.setAdapter(new PhotoGalleryAdapter(mItems));

        mSearchKey = PhotoGalleryPreferance.getStoredSearchKey(getActivity());
        loadPhotos();

        return v;
    }

    /**
     * onCreateOptionsMenu
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_photo_refresh, menu);

        final MenuItem menuSearch = menu.findItem(R.id.menu_item_search_photo);
        final SearchView searchView = (SearchView) menuSearch.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchKey = query;
                loadPhotos();
                Log.d(TAG,"Query Text Submit" + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG,"Query Text Changing" + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery(mSearchKey, false);
            }
        });

        //render polling
        MenuItem menuPolling = menu.findItem(R.id.menu_toggle_polling);
        if (PollService.isServiceAlamOn(getActivity())){
            menuPolling.setTitle(R.string.stop_polling);
        } else {
            menuPolling.setTitle(R.string.start_polling);
        }
    }

    /**
     * onOptionsItemSelected
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_refresh_photo:
                loadPhotos();
                return true;
            case R.id.menu_item_clear_search:
                mSearchKey = null;
                loadPhotos();
                return true;
            case R.id.menu_toggle_polling:
                Log.d(TAG, "Start/Stop Intent Service");
                boolean shouldStart = !PollService.isServiceAlamOn(getActivity());
                /*Intent i = PollService.newIntent(getActivity());
                getActivity().startService(i);*/
                Log.d(TAG, (shouldStart) ?"Start":"Stop" + " Intent Service");
                PollService.setServiceAlarm(getActivity(), shouldStart);
                getActivity().invalidateOptionsMenu(); //refresh menu
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * loadPhotos ขึ้นมาใหม่ ดูจากตัวแปรที่รับมา
     */
    private void loadPhotos() {
        if (mFetcherTask == null || !mFetcherTask.isRunning()) {
            mFetcherTask = new FetcherTask();

            if (mSearchKey != null) {
                mFetcherTask.execute(mSearchKey);
            } else {
                mFetcherTask.execute();
            }
        }
    }

    /**
     * ViewHolder
     */
    class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {

//        TextView mText;
        ImageView mPhoto;
        GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);
            mPhoto = (ImageView) itemView.findViewById(R.id.image_photo);
            mPhoto.setOnClickListener(this);

            itemView.setOnCreateContextMenuListener(this);
        }

        public void bindDrawable(@NonNull Drawable drawable) {
            mPhoto.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            final ImageView imageView = new ImageView(getActivity());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            builder.setView(imageView);
            builder.setPositiveButton(R.string.close, null);
            builder.create().show();

            Log.d(TAG,"mBigUrl : " + mGalleryItem.getBigSizeUrl());

            new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... urls) {
                    FlickrFetcher flickrFetcher = new FlickrFetcher();
                    Bitmap bm = null;
                    try {
                        byte[] bytes = flickrFetcher.getUrlBytes(urls[0]);
                        bm = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    } catch (IOException e) {
                        Log.e(TAG,"error reading bitmap");
                        return null;
                    }
                    return bm;
                }

                @Override
                protected void onPostExecute(Bitmap img) {
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), img));
                }
            }.execute(mGalleryItem.getBigSizeUrl());
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem menuItem = menu.add(0,1,0,R.string.open_with_external_browser);
            menuItem.setOnMenuItemClickListener(this);
            MenuItem menuItem2 = menu.add(0,2,0,R.string.open_with_internal_browser);
            menuItem2.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case 1:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoUri());
                    startActivity(browserIntent);
                    return true;
                case 2:
                    Intent inAppIntent = PhotoPageActivity.newIntent(getActivity(),mGalleryItem.getPhotoUri());
                    startActivity(inAppIntent);
                    return true;
                default:
            }
            return false;
        }

//        public void bindGalleryItem(GalleryItem galleryItem) {
//            mText.setText(galleryItem.getTitle());
//        }
    }

    /**
     * Adapter
     */
    class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoHolder> {

        List<GalleryItem> mGalleryItemList;

        PhotoGalleryAdapter(List<GalleryItem> galleryItems) {
            mGalleryItemList = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_photo,parent,false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
//            holder.bindGalleryItem(mGalleryItemList.get(position));
            Drawable smileyDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.bear, null);

            GalleryItem galleryItem = mGalleryItemList.get(position);
            Log.d(TAG, "bind position # " + position + " , url " + galleryItem.getUrl());

            holder.bindGalleryItem(galleryItem);
            holder.bindDrawable(smileyDrawable);

            if (mMemoryCache.get(galleryItem.getUrl()) != null) {
                Bitmap bitmap = mMemoryCache.get(galleryItem.getUrl());
                holder.bindDrawable(new BitmapDrawable(getResources(), bitmap));
            } else {
                mThumbnailDownloaderThread.queueThumbnailDownloader(holder, galleryItem.getUrl());
            }
            //
        }

        @Override
        public int getItemCount() {
            return mGalleryItemList.size();
        }
    }

    /**
     * AsyncTask
     */
    class FetcherTask extends AsyncTask<String ,Void,List<GalleryItem>> {

        boolean running = false;

        @Override
        protected List<GalleryItem> doInBackground(String ... param) {

            synchronized (this) {
                running = true;
            }

            try {
                Log.d(TAG, "Start Fetcher task ");
                List<GalleryItem> itemList = new ArrayList<>();
                FlickrFetcher flickrFetcher = new FlickrFetcher();

                if (param.length>0) {
                    flickrFetcher.searchPhotos(itemList, param[0]);
                } else {
                    flickrFetcher.getRecentPhotos(itemList);
                    Log.d(TAG, "Fetcher task finished");
                }
                    return itemList;
            } finally {
                synchronized (this) {
                    running = false;
                }
            }
        }

        boolean isRunning() {
            return running;
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
//                mAdapter = new PhotoGalleryAdapter(galleryItems);
            mItems = galleryItems;
            mRecyclerView.setAdapter(new PhotoGalleryAdapter(mItems));

        }
    }
}
