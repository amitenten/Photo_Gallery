package com.augmentis.ayp.photogallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by Amita on 9/5/2016.
 */
public class SettingActivity extends SingleFragmentActivity {
    @Override
    protected Fragment onCreateFragment() {
        return PhotoGallerySetting.newInstance();
    }

    public static Intent newIntent(Context ctx) {
        return new Intent(ctx,SettingActivity.class);
    }

}
