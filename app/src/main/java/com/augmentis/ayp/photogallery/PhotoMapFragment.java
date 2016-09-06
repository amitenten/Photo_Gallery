package com.augmentis.ayp.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

/**
 * Created by Amita on 9/5/2016.
 */
public class PhotoMapFragment extends SupportMapFragment {

    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final String KEY_GALLERY_ITEM = "KEY_GALLERY_ITEM";
    private static final String KEY_BITMAP = "KEY_BITMAP";
    private static final String TAG = "PhotoMapFragment";
    private GoogleMap mGoogleMap;
    private Location mLocation;
    private Location mGalleryItem;
    private String mUrl;
    private Bitmap mBitmap;
    private MarkerFetcherTask mFetcherTask;

    public static PhotoMapFragment newInstance(Location location, Location galleryItemLocation, String url) {

        Bundle args = new Bundle();

        args.putParcelable(KEY_LOCATION, location);
        args.putParcelable(KEY_GALLERY_ITEM, galleryItemLocation);
        args.putString(KEY_BITMAP, url);

        PhotoMapFragment fragment = new PhotoMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mLocation = getArguments().getParcelable(KEY_LOCATION);
            mGalleryItem = getArguments().getParcelable(KEY_GALLERY_ITEM);
            mUrl = getArguments().getString(KEY_BITMAP);
        }

        if (mUrl != null) {
            Log.d(TAG, "onCreate: Get URL: " + mUrl);
        }

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                if (mUrl == null) {
                    updateMapUI();
                } else {
                    mFetcherTask = new MarkerFetcherTask();
                    mFetcherTask.execute(mUrl);
                }
            }
        });
    }

    private void updateMapUI() {
        mGoogleMap.clear();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (mLocation != null) {
            Log.d(TAG, "updateMapUI: Found Location for My Location");
            plotMarker(mLocation, builder);
        }

        if (mGalleryItem != null) {
            Log.d(TAG, "updateMapUI: Found Location for Gallery Items");
            if (mBitmap == null) {
                plotMarker(mGalleryItem, builder);
            } else {
                plotMarker(mGalleryItem, builder, mBitmap);
            }
        }

        int margin = getResources().getDimensionPixelSize(R.dimen.map_insert_margin);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                builder.build(), margin);

        mGoogleMap.animateCamera(cameraUpdate);
    }

    private void plotMarker(final Location location, final LatLngBounds.Builder builder) {
        Log.d(TAG, "plotMaker: Plot Location= " + location);

        LatLng itemPoint = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions itemMarkerOptions = new MarkerOptions().position(itemPoint);
        mGoogleMap.addMarker(itemMarkerOptions);
        builder.include(itemPoint);
    }

    private void plotMarker(final Location location, final LatLngBounds.Builder builder, final Bitmap bitmap) {
        Log.d(TAG, "plotMaker: Plot Location= " + location);

        LatLng itemPoint = new LatLng(location.getLatitude(), location.getLongitude());
        BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
        MarkerOptions itemMarkerOptions = new MarkerOptions()
                .position(itemPoint)
                .icon(itemBitmap);
        mGoogleMap.addMarker(itemMarkerOptions);
        builder.include(itemPoint);
    }

    private class MarkerFetcherTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            //Fetch photo
            String url = params[0];

            try {
                FlickrFetcher flickrFetcher = new FlickrFetcher();
                byte[] imageBytes = flickrFetcher.getUrlBytes(url);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                return bitmap;
            } catch (IOException ioe) {
                Log.e(TAG, "doInBackground: Error in IOE: ", ioe);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mBitmap = bitmap;
            updateMapUI();
            mFetcherTask = null;
        }
    }
}