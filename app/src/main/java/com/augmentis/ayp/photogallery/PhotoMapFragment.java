package com.augmentis.ayp.photogallery;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Amita on 9/5/2016.
 */
public class PhotoMapFragment extends SupportMapFragment {

    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final String KEY_GALLERY_ITEM = "KEY_GALLERY_ITEM";
    private static final String KEY_BITMAP = "KEY_BITMAP";
    private GoogleMap mGoogleMap;

    public static PhotoMapFragment newInstance(Location location, Location galleryItemLocation, Bitmap bitmap) {

        Bundle args = new Bundle();

        args.putParcelable(KEY_LOCATION, location);
        args.putParcelable(KEY_GALLERY_ITEM, galleryItemLocation);
        args.putParcelable(KEY_BITMAP, bitmap);

        PhotoMapFragment fragment = new PhotoMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
            }
        });
    }
}
