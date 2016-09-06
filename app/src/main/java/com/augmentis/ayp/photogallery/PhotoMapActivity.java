package com.augmentis.ayp.photogallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.v4.app.Fragment;

/**
 * Created by Amita on 9/5/2016.
 */
public class PhotoMapActivity extends SingleFragmentActivity {

    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final String KEY_GALLERY_ITEM = "KEY_GALLERY_ITEM";
    private static final String KEY_BITMAP = "KEY_BITMAP";

    @Override
    protected Fragment onCreateFragment() {
        if (getIntent() != null) {
            Location galleryLocation = getIntent().getParcelableExtra(KEY_GALLERY_ITEM);
            Location location = getIntent().getParcelableExtra(KEY_LOCATION);
            String url = getIntent().getStringExtra(KEY_BITMAP);

            return PhotoMapFragment.newInstance(galleryLocation,location,url);
        }

        return PhotoMapFragment.newInstance();
    }
    public static Intent newIntent(Context ctx, Location location,
                                   Location galleryItemLocation, String url) {

        Intent intent = new Intent(ctx, PhotoMapActivity.class);
        intent.putExtra(KEY_LOCATION, location);
        intent.putExtra(KEY_GALLERY_ITEM, galleryItemLocation);
        intent.putExtra(KEY_BITMAP, url);

        return intent;
    }
}
